package com.tientvv.dto.review;

import java.util.UUID;

public class ReplyReviewDto {
    private UUID reviewId;
    private String reply;

    public ReplyReviewDto() {}

    public ReplyReviewDto(UUID reviewId, String reply) {
        this.reviewId = reviewId;
        this.reply = reply;
    }

    public UUID getReviewId() {
        return reviewId;
    }

    public void setReviewId(UUID reviewId) {
        this.reviewId = reviewId;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
