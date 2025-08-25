package com.tientvv.dto.chat;

import com.tientvv.model.ChatMessage;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ChatMessageDto {
    private UUID id;
    private UUID productId;
    private String productName;
    private UUID shopId;
    private String shopName;
    private UUID userId;
    private String userName;
    private ChatMessage.MessageType messageType;
    private String content;
    private Boolean isRead;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String lastMessage;
    private OffsetDateTime lastMessageTime;
}
