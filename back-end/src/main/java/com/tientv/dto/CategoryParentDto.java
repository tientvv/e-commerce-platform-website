package com.tientv.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CategoryParentDto {
    private UUID id;
    private String name;
    private String imageUrl;
}