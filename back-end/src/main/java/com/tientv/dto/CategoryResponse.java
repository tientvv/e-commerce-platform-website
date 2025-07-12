package com.tientv.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CategoryResponse {
    private UUID id;
    private String name;
    private String imageUrl;
    private CategoryParentDto parent;
}

