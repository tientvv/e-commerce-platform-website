package com.tientvv.controller;

import com.tientvv.dto.review.CreateReviewDto;
import com.tientvv.dto.review.ReplyReviewDto;
import com.tientvv.dto.review.ReviewDto;
import com.tientvv.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createReview(@RequestBody CreateReviewDto createReviewDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            ReviewDto review = reviewService.createReview(createReviewDto);
            response.put("success", true);
            response.put("message", "Đánh giá thành công");
            response.put("review", review);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> getReviewsByProduct(@PathVariable UUID productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ReviewDto> reviews = reviewService.getReviewsByProductId(productId);
            response.put("success", true);
            response.put("reviews", reviews);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/product/{productId}/user")
    public ResponseEntity<Map<String, Object>> getUserReviewForProduct(@PathVariable UUID productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            ReviewDto review = reviewService.getReviewByUserAndProduct(productId);
            response.put("success", true);
            response.put("review", review);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/product/{productId}/can-review")
    public ResponseEntity<Map<String, Object>> canUserReviewProduct(@PathVariable UUID productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean hasPurchased = reviewService.hasUserPurchased(productId);
            boolean hasReviewed = reviewService.hasUserReviewed(productId);
            
            response.put("success", true);
            response.put("hasPurchased", hasPurchased);
            response.put("hasReviewed", hasReviewed);
            response.put("canReview", hasPurchased && !hasReviewed);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reply")
    public ResponseEntity<Map<String, Object>> replyToReview(@RequestBody ReplyReviewDto replyReviewDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            ReviewDto review = reviewService.replyToReview(replyReviewDto.getReviewId(), replyReviewDto.getReply());
            response.put("success", true);
            response.put("message", "Trả lời review thành công");
            response.put("review", review);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
