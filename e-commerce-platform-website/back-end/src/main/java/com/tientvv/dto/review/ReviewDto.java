package com.tientvv.dto.review;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ReviewDto {
    private UUID id;
    private UUID productId;
    private String productName;
    private UUID productVariantId;
    private String productVariantName;
    private String productVariantValue;
    private UUID accountId;
    private String accountName;
    private String accountImage;
    private BigDecimal rating;
    private String comment;
    private String reply;
    private OffsetDateTime reviewDate;

    public ReviewDto() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public UUID getProductVariantId() {
        return productVariantId;
    }

    public void setProductVariantId(UUID productVariantId) {
        this.productVariantId = productVariantId;
    }

    public String getProductVariantName() {
        return productVariantName;
    }

    public void setProductVariantName(String productVariantName) {
        this.productVariantName = productVariantName;
    }

    public String getProductVariantValue() {
        return productVariantValue;
    }

    public void setProductVariantValue(String productVariantValue) {
        this.productVariantValue = productVariantValue;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountImage() {
        return accountImage;
    }

    public void setAccountImage(String accountImage) {
        this.accountImage = accountImage;
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

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public OffsetDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(OffsetDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }
}
