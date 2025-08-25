package com.tientvv.controller;

import com.tientvv.dto.chat.ChatMessageDto;
import com.tientvv.dto.chat.CreateChatMessageDto;
import com.tientvv.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ChatMessageDto>> getChatMessagesByProduct(@PathVariable UUID productId) {
        List<ChatMessageDto> messages = chatMessageService.getChatMessagesByProduct(productId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/product/{productId}/user/{userId}")
    public ResponseEntity<List<ChatMessageDto>> getChatMessagesByProductAndUser(
            @PathVariable UUID productId,
            @PathVariable UUID userId) {
        List<ChatMessageDto> messages = chatMessageService.getChatMessagesByProductAndUser(productId, userId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<ChatMessageDto>> getChatMessagesByShop(@PathVariable UUID shopId) {
        List<ChatMessageDto> messages = chatMessageService.getChatMessagesByShop(shopId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatMessageDto>> getChatMessagesByUser(@PathVariable UUID userId) {
        List<ChatMessageDto> messages = chatMessageService.getChatMessagesByUser(userId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/product/{productId}/shop/{shopId}/user/{userId}")
    public ResponseEntity<List<ChatMessageDto>> getChatMessagesByProductAndShopAndUser(
            @PathVariable UUID productId,
            @PathVariable UUID shopId,
            @PathVariable UUID userId) {
        List<ChatMessageDto> messages = chatMessageService.getChatMessagesByProductAndShopAndUser(productId, shopId, userId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @RequestBody CreateChatMessageDto createDto,
            @RequestParam UUID userId) {
        try {
            ChatMessageDto message = chatMessageService.createChatMessage(createDto, userId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            System.err.println("Error creating chat message: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<ChatMessageDto> createMessage(@RequestBody CreateChatMessageDto createDto, @RequestParam UUID userId) {
        try {
            ChatMessageDto message = chatMessageService.createChatMessage(createDto, userId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            System.err.println("Error creating chat message: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/reply/{messageId}")
    public ResponseEntity<ChatMessageDto> replyToMessage(
            @PathVariable UUID messageId,
            @RequestParam String content,
            @RequestParam UUID shopUserId) {
        ChatMessageDto reply = chatMessageService.replyToMessage(messageId, content, shopUserId);
        return ResponseEntity.ok(reply);
    }

    @PutMapping("/read/{messageId}")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable UUID messageId) {
        chatMessageService.markMessageAsRead(messageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread/shop/{shopId}")
    public ResponseEntity<Long> getUnreadMessageCountForShop(@PathVariable UUID shopId) {
        Long count = chatMessageService.getUnreadMessageCountForShop(shopId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/unread/user/{userId}")
    public ResponseEntity<Long> getUnreadMessageCountForUser(@PathVariable UUID userId) {
        Long count = chatMessageService.getUnreadMessageCountForUser(userId);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/conversation/user/{productId}/{shopId}/{userId}")
    public ResponseEntity<Void> hideConversationForUser(
            @PathVariable UUID productId,
            @PathVariable UUID shopId,
            @PathVariable UUID userId) {
        chatMessageService.hideConversationForUser(productId, shopId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/conversation/shop/{productId}/{shopId}/{userId}")
    public ResponseEntity<Void> hideConversationForShop(
            @PathVariable UUID productId,
            @PathVariable UUID shopId,
            @PathVariable UUID userId) {
        chatMessageService.hideConversationForShop(productId, shopId, userId);
        return ResponseEntity.ok().build();
    }
}
