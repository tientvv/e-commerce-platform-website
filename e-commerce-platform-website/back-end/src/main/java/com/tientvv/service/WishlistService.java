package com.tientvv.service;

import com.tientvv.dto.CrudProduct.ProductDisplayDto;
import com.tientvv.model.Account;
import com.tientvv.model.Product;
import com.tientvv.model.ProductVariant;
import com.tientvv.model.Wishlist;
import com.tientvv.repository.AccountRepository;
import com.tientvv.repository.ProductRepository;
import com.tientvv.repository.ProductVariantRepository;
import com.tientvv.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {

	private final WishlistRepository wishlistRepository;
	private final AccountRepository accountRepository;
	private final ProductRepository productRepository;
	private final ProductVariantRepository productVariantRepository;
	private final ProductService productService;

	// Lấy danh sách wishlist của user
	public List<Map<String, Object>> getWishlistItems(UUID accountId) {
		List<Wishlist> wishlistItems = wishlistRepository.findByAccountId(accountId);

		return wishlistItems.stream()
				.map(wishlist -> {
					ProductDisplayDto productDto = productService.convertToProductDisplayDto(wishlist.getProduct());
					Map<String, Object> item = new HashMap<>();
					item.put("id", productDto.getId());
					item.put("name", productDto.getName());
					item.put("brand", productDto.getBrand());
					
					// Nếu có variant, sử dụng giá của variant
					if (wishlist.getProductVariant() != null) {
						item.put("minPrice", wishlist.getProductVariant().getPrice());
						item.put("maxPrice", wishlist.getProductVariant().getPrice());
					} else {
						item.put("minPrice", productDto.getMinPrice());
						item.put("maxPrice", productDto.getMaxPrice());
					}
					
					item.put("productImage", productDto.getProductImage());
					item.put("shopName", productDto.getShopName());
					item.put("discountPercentage", productDto.getDiscountPercentage());
					item.put("discountAmount", productDto.getDiscountAmount());
					item.put("discountType", productDto.getDiscountType());
					item.put("discountName", productDto.getDiscountName());
					item.put("minOrderValue", productDto.getMinOrderValue());
					item.put("variantId", wishlist.getProductVariant() != null ? wishlist.getProductVariant().getId() : null);
					item.put("variantName",
							wishlist.getProductVariant() != null ? wishlist.getProductVariant().getVariantName() : null);
					item.put("variantValue",
							wishlist.getProductVariant() != null ? wishlist.getProductVariant().getVariantValue() : null);
					return item;
				})
				.collect(Collectors.toList());
	}

	// Thêm sản phẩm vào wishlist
	public void addToWishlist(UUID accountId, UUID productId, UUID variantId) {
		System.out.println("=== WishlistService.addToWishlist ===");
		System.out.println("Account ID: " + accountId);
		System.out.println("Product ID: " + productId);
		System.out.println("Variant ID: " + variantId);

		// Kiểm tra xem đã có trong wishlist chưa
		boolean exists = wishlistRepository.existsByAccountIdAndProductIdAndVariantId(accountId, productId, variantId);
		System.out.println("Already exists in wishlist: " + exists);

		if (exists) {
			throw new RuntimeException("Sản phẩm đã có trong danh sách yêu thích");
		}

		System.out.println("Finding account...");
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new RuntimeException("Account not found"));
		System.out.println("Account found: " + account.getName());

		System.out.println("Finding product...");
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Product not found"));
		System.out.println("Product found: " + product.getName());

		System.out.println("Finding variant...");
		ProductVariant variant = null;
		if (variantId != null) {
			variant = productVariantRepository.findById(variantId)
					.orElseThrow(() -> new RuntimeException("Product variant not found"));
			System.out.println("Variant found: " + variant.getVariantName());
		}

		System.out.println("Creating wishlist item...");
		Wishlist wishlist = new Wishlist();
		wishlist.setAccount(account);
		wishlist.setProduct(product);
		wishlist.setProductVariant(variant);
		wishlist.setCreatedAt(OffsetDateTime.now());

		wishlistRepository.save(wishlist);
		System.out.println("Wishlist item saved successfully");
	}

	// Xóa sản phẩm khỏi wishlist
	public void removeFromWishlist(UUID accountId, UUID productId, UUID variantId) {
		wishlistRepository.deleteByAccountIdAndProductIdAndVariantId(accountId, productId, variantId);
	}

	// Kiểm tra sản phẩm có trong wishlist không
	public boolean isInWishlist(UUID accountId, UUID productId, UUID variantId) {
		return wishlistRepository.existsByAccountIdAndProductIdAndVariantId(accountId, productId, variantId);
	}

	// Toggle wishlist (thêm nếu chưa có, xóa nếu đã có)
	public boolean toggleWishlist(UUID accountId, UUID productId, UUID variantId) {
		System.out.println("=== WishlistService.toggleWishlist ===");
		System.out.println("Account ID: " + accountId);
		System.out.println("Product ID: " + productId);
		System.out.println("Variant ID: " + variantId);

		boolean isInWishlist = isInWishlist(accountId, productId, variantId);
		System.out.println("Is in wishlist: " + isInWishlist);

		if (isInWishlist) {
			System.out.println("Removing from wishlist...");
			removeFromWishlist(accountId, productId, variantId);
			return false; // Đã bỏ yêu thích
		} else {
			System.out.println("Adding to wishlist...");
			addToWishlist(accountId, productId, variantId);
			return true; // Đã thêm yêu thích
		}
	}

	// Lấy số lượng wishlist cho một sản phẩm
	public long getWishlistCount(UUID productId) {
		return wishlistRepository.countByProductId(productId);
	}
}
