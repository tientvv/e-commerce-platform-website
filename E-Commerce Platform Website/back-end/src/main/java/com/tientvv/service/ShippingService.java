package com.tientvv.service;

import com.tientvv.dto.shipping.CreateShippingDto;
import com.tientvv.dto.shipping.ShippingDto;
import com.tientvv.dto.shipping.UpdateShippingDto;
import com.tientvv.model.Shipping;
import com.tientvv.repository.ShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShippingService {

  private static final Logger logger = LoggerFactory.getLogger(ShippingService.class);

  @Autowired
  private ShippingRepository shippingRepository;

  public List<ShippingDto> getAllShippings() {
    try {
      logger.info("Fetching all active shippings");
      List<Shipping> shippings = shippingRepository.findByIsActiveTrueOrderByShippingMethod();
      logger.info("Found {} active shippings", shippings.size());
      return shippings.stream()
          .map(ShippingDto::fromEntity)
          .collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error fetching active shippings: {}", e.getMessage(), e);
      throw new RuntimeException("Không thể lấy danh sách vận chuyển: " + e.getMessage());
    }
  }

  public List<ShippingDto> getAllShippingsIncludingInactive() {
    try {
      logger.info("Fetching all shippings including inactive");
      List<Shipping> shippings = shippingRepository.findAll();
      logger.info("Found {} total shippings", shippings.size());
      return shippings.stream()
          .map(ShippingDto::fromEntity)
          .collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error fetching all shippings: {}", e.getMessage(), e);
      throw new RuntimeException("Không thể lấy danh sách vận chuyển: " + e.getMessage());
    }
  }

  public ShippingDto getShippingById(UUID id) {
    try {
      logger.info("Fetching shipping by id: {}", id);
      Shipping shipping = shippingRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức vận chuyển với id: " + id));
      return ShippingDto.fromEntity(shipping);
    } catch (Exception e) {
      logger.error("Error fetching shipping by id {}: {}", id, e.getMessage(), e);
      throw new RuntimeException("Không thể lấy thông tin vận chuyển: " + e.getMessage());
    }
  }

  public ShippingDto createShipping(CreateShippingDto createShippingDto) {
    try {
      logger.info("Creating new shipping: {}", createShippingDto.getShippingMethod());
      Shipping shipping = new Shipping();
      shipping.setShippingMethod(createShippingDto.getShippingMethod());
      shipping.setPrice(createShippingDto.getPrice());
      shipping.setEstimatedDelivery(createShippingDto.getEstimatedDelivery());
      shipping.setIsActive(createShippingDto.getIsActive() != null ? createShippingDto.getIsActive() : true);

      Shipping savedShipping = shippingRepository.save(shipping);
      logger.info("Successfully created shipping with id: {}", savedShipping.getId());
      return ShippingDto.fromEntity(savedShipping);
    } catch (Exception e) {
      logger.error("Error creating shipping: {}", e.getMessage(), e);
      throw new RuntimeException("Không thể tạo phương thức vận chuyển: " + e.getMessage());
    }
  }

  public ShippingDto updateShipping(UUID id, UpdateShippingDto updateShippingDto) {
    try {
      logger.info("Updating shipping with id: {}", id);
      Shipping shipping = shippingRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức vận chuyển với id: " + id));

      shipping.setShippingMethod(updateShippingDto.getShippingMethod());
      shipping.setPrice(updateShippingDto.getPrice());
      shipping.setEstimatedDelivery(updateShippingDto.getEstimatedDelivery());
      shipping.setIsActive(updateShippingDto.getIsActive());

      Shipping updatedShipping = shippingRepository.save(shipping);
      logger.info("Successfully updated shipping with id: {}", id);
      return ShippingDto.fromEntity(updatedShipping);
    } catch (Exception e) {
      logger.error("Error updating shipping with id {}: {}", id, e.getMessage(), e);
      throw new RuntimeException("Không thể cập nhật phương thức vận chuyển: " + e.getMessage());
    }
  }

  public void deleteShipping(UUID id) {
    try {
      logger.info("Deleting shipping with id: {}", id);
      Shipping shipping = shippingRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức vận chuyển với id: " + id));

      // Check if shipping is used in any orders
      if (!shipping.getOrders().isEmpty()) {
        throw new RuntimeException("Không thể xóa phương thức vận chuyển đã được sử dụng trong đơn hàng.");
      }

      shippingRepository.delete(shipping);
      logger.info("Successfully deleted shipping with id: {}", id);
    } catch (Exception e) {
      logger.error("Error deleting shipping with id {}: {}", id, e.getMessage(), e);
      throw new RuntimeException("Không thể xóa phương thức vận chuyển: " + e.getMessage());
    }
  }
}