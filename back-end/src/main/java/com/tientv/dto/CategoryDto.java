package com.tientv.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CategoryDto {
    private String name;
    private UUID parentId;
    private String imageUrl;
}
