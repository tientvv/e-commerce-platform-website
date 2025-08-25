package com.tientvv.dto.chat;

import com.tientvv.model.ChatMessage;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateChatMessageDto {
    private UUID productId;
    private UUID shopId;
    private ChatMessage.MessageType messageType;
    private String content;
}
