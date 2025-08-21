package com.tientvv.service;

import com.tientvv.dto.review.CreateReviewDto;
import com.tientvv.dto.review.ReviewDto;
import com.tientvv.model.*;
import com.tientvv.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @SuppressWarnings("unused")
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AccountService accountService;

    @Transactional
    public ReviewDto createReview(CreateReviewDto createReviewDto) {
        // Kiểm tra user hiện tại
        Account currentUser = accountService.getCurrentUserFromSession();
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }

        // Kiểm tra sản phẩm tồn tại
        Product product = productRepository.findById(createReviewDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Kiểm tra variant nếu có
        ProductVariant productVariant = null;
        if (createReviewDto.getProductVariantId() != null) {
            productVariant = productVariantRepository.findById(createReviewDto.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException("Product variant not found"));
        }

        // Kiểm tra user đã mua sản phẩm này chưa
        boolean hasPurchased = orderRepository.existsByAccountAndProduct(currentUser.getId(), product.getId());
        if (!hasPurchased) {
            throw new RuntimeException("Bạn chưa mua sản phẩm này");
        }

        // Kiểm tra user đã đánh giá sản phẩm này chưa
        boolean hasReviewed = reviewRepository.existsByAccountAndProduct(currentUser.getId(), product.getId());
        if (hasReviewed) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        }

        // Tạo review
        Review review = new Review();
        review.setProduct(product);
        review.setProductVariant(productVariant);
        review.setAccount(currentUser);
        review.setRating(BigDecimal.valueOf(createReviewDto.getRating().doubleValue()));
        review.setComment(createReviewDto.getComment());
        review.setReviewDate(OffsetDateTime.now());

        Review savedReview = reviewRepository.save(review);

        return convertToDto(savedReview);
    }

    public List<ReviewDto> getReviewsByProductId(UUID productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByReviewDateDesc(productId);
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ReviewDto getReviewByUserAndProduct(UUID productId) {
        Account currentUser = accountService.getCurrentUserFromSession();
        if (currentUser == null) {
            return null;
        }

        Review review = reviewRepository.findByAccountAndProduct(currentUser.getId(), productId);
        return review != null ? convertToDto(review) : null;
    }

    public boolean hasUserReviewed(UUID productId) {
        Account currentUser = accountService.getCurrentUserFromSession();
        if (currentUser == null) {
            return false;
        }

        return reviewRepository.existsByAccountAndProduct(currentUser.getId(), productId);
    }

    public boolean hasUserPurchased(UUID productId) {
        Account currentUser = accountService.getCurrentUserFromSession();
        if (currentUser == null) {
            return false;
        }

        return orderRepository.existsByAccountAndProduct(currentUser.getId(), productId);
    }

    @Transactional
    public ReviewDto replyToReview(UUID reviewId, String reply) {
        // Kiểm tra user hiện tại
        Account currentUser = accountService.getCurrentUserFromSession();
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }

        // Tìm review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Kiểm tra user có phải là chủ shop của sản phẩm này không
        if (!review.getProduct().getShop().getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền trả lời review này");
        }

        // Cập nhật reply
        review.setReply(reply);
        Review savedReview = reviewRepository.save(review);

        return convertToDto(savedReview);
    }

    @Transactional
    private void updateProductRating(UUID productId) {
        BigDecimal avgRating = reviewRepository.getAverageRatingByProductId(productId);
        if (avgRating != null) {
            // Cập nhật rating trong Product (nếu có field rating)
            // Hoặc tính toán rating động khi query
        }
    }

    private ReviewDto convertToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getName());
        
        if (review.getProductVariant() != null) {
            dto.setProductVariantId(review.getProductVariant().getId());
            dto.setProductVariantName(review.getProductVariant().getVariantName());
            dto.setProductVariantValue(review.getProductVariant().getVariantValue());
        }
        
        dto.setAccountId(review.getAccount().getId());
        dto.setAccountName(review.getAccount().getName() != null ? 
                review.getAccount().getName() : review.getAccount().getUsername());
        dto.setAccountImage(review.getAccount().getAccountsImage());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setReply(review.getReply());
        dto.setReviewDate(review.getReviewDate());
        
        return dto;
    }
}
