package com.tientvv.service;

import com.tientvv.dto.chat.ChatMessageDto;
import com.tientvv.dto.chat.CreateChatMessageDto;
import com.tientvv.model.Account;
import com.tientvv.model.ChatMessage;
import com.tientvv.model.Product;
import com.tientvv.model.Shop;
import com.tientvv.repository.AccountRepository;
import com.tientvv.repository.ChatMessageRepository;
import com.tientvv.repository.ProductRepository;
import com.tientvv.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Map;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private AccountRepository accountRepository;

    public List<ChatMessageDto> getChatMessagesByProduct(UUID productId) {
        List<ChatMessage> messages = chatMessageRepository.findByProductIdOrderByCreatedAtAsc(productId);
        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<ChatMessageDto> getChatMessagesByProductAndUser(UUID productId, UUID userId) {
        System.out.println("Getting chat messages for product: " + productId + " and user: " + userId);
        
        // Load tất cả tin nhắn trong cuộc trò chuyện này (cả customer và shop reply)
        List<ChatMessage> allMessages = chatMessageRepository.findByProductIdOrderByCreatedAtAsc(productId);
        
        // Lọc chỉ tin nhắn liên quan đến customer này
        List<ChatMessage> conversationMessages = allMessages.stream()
                .filter(message -> {
                    // Nếu là tin nhắn customer, kiểm tra userId
                    if (message.getMessageType() == ChatMessage.MessageType.CUSTOMER_MESSAGE) {
                        return message.getUser().getId().equals(userId);
                    }
                    // Nếu là tin nhắn shop reply, kiểm tra xem có tin nhắn customer của userId này không
                    if (message.getMessageType() == ChatMessage.MessageType.SHOP_REPLY) {
                        return allMessages.stream()
                                .anyMatch(customerMsg -> 
                                    customerMsg.getMessageType() == ChatMessage.MessageType.CUSTOMER_MESSAGE &&
                                    customerMsg.getUser().getId().equals(userId) &&
                                    customerMsg.getProduct().getId().equals(message.getProduct().getId())
                                );
                    }
                    return false;
                })
                .collect(Collectors.toList());
        
        // Tự động đánh dấu tin nhắn đã đọc cho user
        List<ChatMessage> messagesToMarkAsRead = conversationMessages.stream()
                .filter(message -> {
                    // Đánh dấu SHOP_REPLY là đã đọc cho user
                    if (message.getMessageType() == ChatMessage.MessageType.SHOP_REPLY && 
                        !message.getIsRead()) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        
        // Lưu tin nhắn đã đánh dấu đọc
        for (ChatMessage message : messagesToMarkAsRead) {
            message.setIsRead(true);
        }
        chatMessageRepository.saveAll(messagesToMarkAsRead);
        
        System.out.println("Found " + conversationMessages.size() + " messages in conversation");
        return conversationMessages.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<ChatMessageDto> getChatMessagesByShop(UUID shopId) {
        System.out.println("Getting chat messages for shop: " + shopId);
        List<ChatMessage> messages = chatMessageRepository.findByShopIdOrderByCreatedAtDesc(shopId);
        System.out.println("Found " + messages.size() + " messages for shop");
        
        // Lọc tin nhắn chưa bị ẩn bởi shop
        List<ChatMessage> visibleMessages = messages.stream()
                .filter(message -> !message.getIsDeletedByShop())
                .collect(Collectors.toList());
        
        if (visibleMessages.isEmpty()) {
            System.out.println("No visible messages found for shop: " + shopId);
            return new ArrayList<>();
        }
        
        // Group theo product và customer (không phải user của tin nhắn)
        List<ChatMessageDto> result = visibleMessages.stream()
                .collect(Collectors.groupingBy(
                    message -> {
                        // Nếu là tin nhắn của shop, tìm customer từ tin nhắn customer gần nhất
                        if (message.getMessageType() == ChatMessage.MessageType.SHOP_REPLY) {
                            // Tìm tin nhắn customer trong cùng cuộc trò chuyện
                            ChatMessage customerMessage = visibleMessages.stream()
                                    .filter(msg -> msg.getProduct().getId().equals(message.getProduct().getId()) &&
                                                 msg.getMessageType() == ChatMessage.MessageType.CUSTOMER_MESSAGE)
                                    .findFirst()
                                    .orElse(null);
                            if (customerMessage != null) {
                                return message.getProduct().getId() + "_" + customerMessage.getUser().getId();
                            }
                        }
                        // Nếu là tin nhắn customer, group theo customer
                        return message.getProduct().getId() + "_" + message.getUser().getId();
                    },
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        messageList -> {
                            ChatMessage latestMessage = messageList.stream()
                                    .max(Comparator.comparing(ChatMessage::getCreatedAt))
                                    .orElse(null);
                            if (latestMessage != null) {
                                // Tìm customer từ tin nhắn customer trong group này
                                ChatMessage customerMessage = messageList.stream()
                                        .filter(msg -> msg.getMessageType() == ChatMessage.MessageType.CUSTOMER_MESSAGE)
                                        .findFirst()
                                        .orElse(null);
                                
                                if (customerMessage != null) {
                                    // Tạo DTO với thông tin customer
                                    ChatMessageDto dto = convertToDto(latestMessage);
                                    // Override userId và userName với thông tin customer
                                    dto.setUserId(customerMessage.getUser().getId());
                                    dto.setUserName(customerMessage.getUser().getName());
                                    
                                    // Kiểm tra xem có tin nhắn chưa đọc không
                                    boolean hasUnread = messageList.stream()
                                            .anyMatch(msg -> msg.getMessageType() == ChatMessage.MessageType.CUSTOMER_MESSAGE && !msg.getIsRead());
                                    dto.setIsRead(!hasUnread);
                                    
                                    System.out.println("Created DTO for conversation: " + dto.getProductName() + " - " + dto.getUserName());
                                    return dto;
                                }
                            }
                            return null;
                        }
                    )
                ))
                .values()
                .stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ChatMessageDto::getCreatedAt).reversed())
                .collect(Collectors.toList());
        
        System.out.println("Returning " + result.size() + " conversations for shop: " + shopId);
        return result;
    }

    public List<ChatMessageDto> getChatMessagesByUser(UUID userId) {
        System.out.println("Getting chat messages for user: " + userId);
        
        // Lấy tất cả tin nhắn của user này (chưa bị ẩn bởi user)
        List<ChatMessage> userMessages = chatMessageRepository.findByUserIdOrderByCreatedAtDesc(userId);
        System.out.println("Found " + userMessages.size() + " user messages");
        
        // Lọc tin nhắn chưa bị ẩn bởi user
        List<ChatMessage> visibleMessages = userMessages.stream()
                .filter(message -> !message.getIsDeletedByUser())
                .collect(Collectors.toList());
        
        // Group theo product và shop để tạo danh sách cuộc trò chuyện
        Map<String, List<ChatMessage>> conversationGroups = visibleMessages.stream()
                .collect(Collectors.groupingBy(
                    message -> message.getProduct().getId() + "_" + message.getShop().getId()
                ));
        
        List<ChatMessageDto> result = new ArrayList<>();
        
        for (List<ChatMessage> conversation : conversationGroups.values()) {
            if (!conversation.isEmpty()) {
                // Lấy tin nhắn mới nhất trong cuộc trò chuyện
                ChatMessage latestMessage = conversation.stream()
                        .max(Comparator.comparing(ChatMessage::getCreatedAt))
                        .orElse(null);
                
                if (latestMessage != null) {
                    ChatMessageDto dto = convertToDto(latestMessage);
                    
                    // Kiểm tra xem có tin nhắn chưa đọc không
                    boolean hasUnread = conversation.stream()
                            .anyMatch(msg -> msg.getMessageType() == ChatMessage.MessageType.SHOP_REPLY && !msg.getIsRead());
                    dto.setIsRead(!hasUnread);
                    
                    result.add(dto);
                }
            }
        }
        
        // Sắp xếp theo thời gian tin nhắn mới nhất
        result.sort(Comparator.comparing(ChatMessageDto::getCreatedAt).reversed());
        
        System.out.println("Returning " + result.size() + " conversations for user: " + userId);
        return result;
    }

    public List<ChatMessageDto> getChatMessagesByProductAndShopAndUser(UUID productId, UUID shopId, UUID userId) {
        System.out.println("Getting chat messages for product: " + productId + ", shop: " + shopId + ", user: " + userId);
        
        // Load tất cả tin nhắn trong cuộc trò chuyện này
        List<ChatMessage> allMessages = chatMessageRepository.findByProductIdOrderByCreatedAtAsc(productId);
        
        // Lọc chỉ tin nhắn liên quan đến customer và shop này (chưa bị ẩn)
        List<ChatMessage> conversationMessages = allMessages.stream()
                .filter(message -> {
                    // Kiểm tra tin nhắn chưa bị ẩn
                    if (message.getIsDeletedByUser() || message.getIsDeletedByShop()) {
                        return false;
                    }
                    
                    // Nếu là tin nhắn customer, kiểm tra userId
                    if (message.getMessageType() == ChatMessage.MessageType.CUSTOMER_MESSAGE) {
                        return message.getUser().getId().equals(userId);
                    }
                    // Nếu là tin nhắn shop reply, kiểm tra shopId
                    if (message.getMessageType() == ChatMessage.MessageType.SHOP_REPLY) {
                        return message.getShop().getId().equals(shopId);
                    }
                    return false;
                })
                .collect(Collectors.toList());
        
        // Tự động đánh dấu tin nhắn đã đọc
        List<ChatMessage> messagesToMarkAsRead = conversationMessages.stream()
                .filter(message -> {
                    // Đánh dấu SHOP_REPLY là đã đọc cho user
                    if (message.getMessageType() == ChatMessage.MessageType.SHOP_REPLY && 
                        !message.getIsRead()) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        
        // Lưu tin nhắn đã đánh dấu đọc
        for (ChatMessage message : messagesToMarkAsRead) {
            message.setIsRead(true);
        }
        chatMessageRepository.saveAll(messagesToMarkAsRead);
        
        System.out.println("Found " + conversationMessages.size() + " messages in conversation");
        return conversationMessages.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public ChatMessageDto createChatMessage(CreateChatMessageDto createDto, UUID userId) {
        ChatMessage chatMessage = new ChatMessage();
        
        Product product = productRepository.findById(createDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        Shop shop = shopRepository.findById(createDto.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
        
        Account user = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        chatMessage.setProduct(product);
        chatMessage.setShop(shop);
        chatMessage.setUser(user);
        chatMessage.setMessageType(createDto.getMessageType());
        chatMessage.setContent(createDto.getContent());
        chatMessage.setIsRead(false);

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return convertToDto(savedMessage);
    }

    public ChatMessageDto replyToMessage(UUID messageId, String replyContent, UUID shopUserId) {
        ChatMessage originalMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Tin nhắn không tồn tại"));

        ChatMessage replyMessage = new ChatMessage();
        replyMessage.setProduct(originalMessage.getProduct());
        replyMessage.setShop(originalMessage.getShop());
        replyMessage.setUser(originalMessage.getShop().getUser());
        replyMessage.setMessageType(ChatMessage.MessageType.SHOP_REPLY);
        replyMessage.setContent(replyContent);
        replyMessage.setIsRead(false);

        ChatMessage savedReply = chatMessageRepository.save(replyMessage);
        return convertToDto(savedReply);
    }

    public void markMessageAsRead(UUID messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Tin nhắn không tồn tại"));
        message.setIsRead(true);
        chatMessageRepository.save(message);
    }

    public Long getUnreadMessageCountForShop(UUID shopId) {
        // Lấy tất cả tin nhắn chưa đọc của shop (chỉ CUSTOMER_MESSAGE)
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessagesByShopId(shopId);
        
        // Lọc tin nhắn chưa bị ẩn bởi shop và chỉ đếm tin nhắn từ customer
        long count = unreadMessages.stream()
                .filter(message -> !message.getIsDeletedByShop() && 
                                 message.getMessageType() == ChatMessage.MessageType.CUSTOMER_MESSAGE)
                .count();
        
        return count;
    }

    public Long getUnreadMessageCountForUser(UUID userId) {
        // Lấy tất cả tin nhắn chưa đọc của user (chỉ SHOP_REPLY)
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessagesByUserId(userId);
        
        // Lọc tin nhắn chưa bị ẩn bởi user và chỉ đếm tin nhắn từ shop
        long count = unreadMessages.stream()
                .filter(message -> !message.getIsDeletedByUser() && 
                                 message.getMessageType() == ChatMessage.MessageType.SHOP_REPLY)
                .count();
        
        return count;
    }

    public void hideConversationForUser(UUID productId, UUID shopId, UUID userId) {
        System.out.println("Hiding conversation for user - product: " + productId + ", shop: " + shopId + ", user: " + userId);
        
        // Lấy tất cả tin nhắn trong cuộc trò chuyện này
        List<ChatMessage> allMessages = chatMessageRepository.findByProductIdOrderByCreatedAtAsc(productId);
        
        // Tìm tất cả tin nhắn trong cuộc trò chuyện giữa customer và shop này
        List<ChatMessage> conversationMessages = allMessages.stream()
                .filter(message -> {
                    // Nếu là tin nhắn customer, kiểm tra userId và shopId
                    if (message.getMessageType() == ChatMessage.MessageType.CUSTOMER_MESSAGE) {
                        return message.getUser().getId().equals(userId) && message.getShop().getId().equals(shopId);
                    }
                    // Nếu là tin nhắn shop reply, kiểm tra shopId và có tin nhắn customer tương ứng không
                    if (message.getMessageType() == ChatMessage.MessageType.SHOP_REPLY) {
                        return message.getShop().getId().equals(shopId) && 
                               allMessages.stream()
                                   .anyMatch(customerMsg -> 
                                       customerMsg.getMessageType() == ChatMessage.MessageType.CUSTOMER_MESSAGE &&
                                       customerMsg.getUser().getId().equals(userId) &&
                                       customerMsg.getShop().getId().equals(shopId)
                                   );
                    }
                    return false;
                })
                .collect(Collectors.toList());
        
        System.out.println("Found " + conversationMessages.size() + " messages to hide for user");
        
        // Đánh dấu ẩn cho user
        for (ChatMessage message : conversationMessages) {
            message.setIsDeletedByUser(true);
        }
        
        chatMessageRepository.saveAll(conversationMessages);
        System.out.println("Successfully hidden conversation for user");
    }

    public void hideConversationForShop(UUID productId, UUID shopId, UUID userId) {
        System.out.println("Hiding conversation for shop - product: " + productId + ", shop: " + shopId + ", user: " + userId);
        
        // Lấy tất cả tin nhắn trong cuộc trò chuyện này
        List<ChatMessage> allMessages = chatMessageRepository.findByProductIdOrderByCreatedAtAsc(productId);
        
        // Tìm tất cả tin nhắn trong cuộc trò chuyện giữa customer và shop này
        List<ChatMessage> conversationMessages = allMessages.stream()
                .filter(message -> {
                    // Nếu là tin nhắn customer, kiểm tra userId và shopId
                    if (message.getMessageType() == ChatMessage.MessageType.CUSTOMER_MESSAGE) {
                        return message.getUser().getId().equals(userId) && message.getShop().getId().equals(shopId);
                    }
                    // Nếu là tin nhắn shop reply, kiểm tra shopId và có tin nhắn customer tương ứng không
                    if (message.getMessageType() == ChatMessage.MessageType.SHOP_REPLY) {
                        return message.getShop().getId().equals(shopId) && 
                               allMessages.stream()
                                   .anyMatch(customerMsg -> 
                                       customerMsg.getMessageType() == ChatMessage.MessageType.CUSTOMER_MESSAGE &&
                                       customerMsg.getUser().getId().equals(userId) &&
                                       customerMsg.getShop().getId().equals(shopId)
                                   );
                    }
                    return false;
                })
                .collect(Collectors.toList());
        
        System.out.println("Found " + conversationMessages.size() + " messages to hide for shop");
        
        // Đánh dấu ẩn cho shop
        for (ChatMessage message : conversationMessages) {
            message.setIsDeletedByShop(true);
        }
        
        chatMessageRepository.saveAll(conversationMessages);
        System.out.println("Successfully hidden conversation for shop");
    }

    private ChatMessageDto convertToDto(ChatMessage chatMessage) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(chatMessage.getId());
        dto.setProductId(chatMessage.getProduct().getId());
        dto.setProductName(chatMessage.getProduct().getName());
        dto.setShopId(chatMessage.getShop().getId());
        dto.setShopName(chatMessage.getShop().getShopName());
        dto.setUserId(chatMessage.getUser().getId());
        dto.setUserName(chatMessage.getUser().getName());
        dto.setMessageType(chatMessage.getMessageType());
        dto.setContent(chatMessage.getContent());
        dto.setIsRead(chatMessage.getIsRead());
        dto.setCreatedAt(chatMessage.getCreatedAt());
        dto.setUpdatedAt(chatMessage.getUpdatedAt());
        dto.setLastMessage(chatMessage.getContent());
        dto.setLastMessageTime(chatMessage.getCreatedAt());
        return dto;
    }
}
