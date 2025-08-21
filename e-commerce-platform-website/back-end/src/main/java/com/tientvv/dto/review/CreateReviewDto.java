package com.tientvv.dto.review;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateReviewDto {
    private UUID productId;
    private UUID productVariantId;
    private BigDecimal rating;
    private String comment;

    public CreateReviewDto() {}

    public CreateReviewDto(UUID productId, UUID productVariantId, BigDecimal rating, String comment) {
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.rating = rating;
        this.comment = comment;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getProductVariantId() {
        return productVariantId;
    }

    public void setProductVariantId(UUID productVariantId) {
        this.productVariantId = productVariantId;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
