package com.example.productservice.service;

import com.example.productservice.dto.*;
import com.example.productservice.entity.Product;
import com.example.productservice.exception.OutOfStockException;
import com.example.productservice.exception.ResourceNotFoundException;
import com.example.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repo;
    private final RedissonClient redissonClient;

    private ProductResponseDTO toDto(Product p) {
        return ProductResponseDTO.builder().id(p.getId()).name(p.getName()).price(p.getPrice()).stockQuantity(p.getStockQuantity()).build();
    }

    public ProductResponseDTO create(ProductRequestDTO dto) {
        Product p = Product.builder().name(dto.getName()).price(dto.getPrice()).stockQuantity(dto.getStockQuantity()).build();
        return toDto(repo.save(p));
    }

    /**
     * Chi tiet san pham - hot path. Lan dau doc DB (~200ms), cac lan sau doc
     * Redis (&lt;10ms). TTL 30 phut cau hinh trong RedisCacheConfig.
     */
    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public ProductResponseDTO findById(Long id) {
        Product p = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toDto(p);
    }

    public List<ProductResponseDTO> findAll() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    /**
     * Admin cap nhat thong tin san pham -> tu dong xoa cache {@code products::id}
     * de lan GET ke tiep doc DB moi va nap lai Redis (TTL 30 phut).
     */
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        Product p = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        p.setName(dto.getName());
        p.setPrice(dto.getPrice());
        p.setStockQuantity(dto.getStockQuantity());
        return toDto(repo.save(p));
    }

    /**
     * Xoa san pham -> xoa luon cache {@code products::id}.
     */
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void delete(Long id) {
        Product p = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        repo.delete(p);
    }

    /**
     * Mua hang Flash Sale: giang khoa phan tan {@code lock:product:{id}} roi moi
     * duoc kiem tra ton kho va tru so luong -> 100 nguoi tranh 1 mon thi chi
     * 1 nguoi thanh cong, con lai nhan "San pham da het hang".
     * Khoa luon duoc giai phong trong {@code finally} (tranh treo he thong).
     *
     * <p>Luu y: KHONG dung @Transactional o day. Ghi bang {@code saveAndFlush}
     * de commit NGAY KHI VAN GIU KHOA. Neu unlock truoc commit, thread ke tiep
     * se doc du lieu cu (chua commit) -> van oversell du co lock.
     */
    @CacheEvict(value = "products", key = "#id")
    public ProductResponseDTO purchase(Long id, Integer quantity) {
        if (quantity == null || quantity <= 0) throw new IllegalArgumentException("quantity must be greater than 0");
        RLock lock = redissonClient.getLock("lock:product:" + id);
        lock.lock();
        try {
            Product p = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
            if (p.getStockQuantity() == null || p.getStockQuantity() < quantity) {
                throw new OutOfStockException("Sản phẩm đã hết hàng");
            }
            p.setStockQuantity(p.getStockQuantity() - quantity);
            return toDto(repo.saveAndFlush(p));
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
