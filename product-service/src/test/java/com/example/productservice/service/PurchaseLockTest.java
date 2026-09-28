package com.example.productservice.service;

import com.example.productservice.dto.ProductResponseDTO;
import com.example.productservice.entity.Product;
import com.example.productservice.exception.OutOfStockException;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Khoa phan tan mua hang (Mockito, khong can Redis that).
 * - Giang lock:product:{id} -> check ton -> tru -> unlock trong finally.
 * - Het hang -> OutOfStockException, khong save, van unlock.
 */
@ExtendWith(MockitoExtension.class)
class PurchaseLockTest {

    @Mock ProductRepository repo;
    @Mock RedissonClient redissonClient;
    @Mock RLock lock;

    @InjectMocks ProductService service;

    private Product productWithStock(int stock) {
        return Product.builder().id(1L).name("Flash").price(10000L).stockQuantity(stock).build();
    }

    private void stubLock() {
        when(redissonClient.getLock("lock:product:1")).thenReturn(lock);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
    }

    @Test
    void purchase_acquiresLockDecrementsAndUnlocks() {
        stubLock();
        when(repo.findById(1L)).thenReturn(Optional.of(productWithStock(5)));
        when(repo.saveAndFlush(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductResponseDTO res = service.purchase(1L, 2);

        assertThat(res.getStockQuantity()).isEqualTo(3);
        verify(lock).lock();
        verify(lock).unlock();
    }

    @Test
    void purchase_outOfStock_throwsNoSaveButUnlocks() {
        stubLock();
        when(repo.findById(1L)).thenReturn(Optional.of(productWithStock(1)));

        assertThatThrownBy(() -> service.purchase(1L, 2))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining("hết hàng");

        verify(repo, never()).saveAndFlush(any(Product.class));
        verify(lock).unlock();
    }

    @Test
    void purchase_dbFailsMidWay_stillUnlocks() {
        stubLock();
        when(repo.findById(1L)).thenReturn(Optional.of(productWithStock(5)));
        when(repo.saveAndFlush(any(Product.class))).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.purchase(1L, 1))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("db down");

        verify(lock).unlock();
    }

    @Test
    void purchase_invalidQuantity_rejectsWithoutLock() {
        assertThatThrownBy(() -> service.purchase(1L, 0))
                .isInstanceOf(IllegalArgumentException.class);

        verify(redissonClient, never()).getLock(anyString());
    }
}
