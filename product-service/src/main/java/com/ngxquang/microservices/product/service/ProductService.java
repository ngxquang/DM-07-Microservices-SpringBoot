package com.ngxquang.microservices.product.service;


import com.ngxquang.microservices.product.dto.ProductRequest;
import com.ngxquang.microservices.product.dto.ProductResponse;
import com.ngxquang.microservices.product.model.Product;
import com.ngxquang.microservices.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_KEY = "product::all";

    @CacheEvict(value = "product", key = "'all'")
    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.name())
                .description(productRequest.description())
                .skuCode(productRequest.skuCode())
                .price(productRequest.price())
                .build();

        productRepository.save(product);
        log.info("Create product successfully");
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(),
                product.getSkuCode(),
                product.getPrice());
    }

    //    @Cacheable(value = "products")
    public List<ProductResponse> getAllProduct() {
        long start = System.currentTimeMillis();

        List<ProductResponse> cachedProducts = (List<ProductResponse>) redisTemplate.opsForValue().get(CACHE_KEY);
        if (cachedProducts != null) {
            long end = System.currentTimeMillis();
            log.info("CACHE HIT for {} (Time: {} ms)", CACHE_KEY, end - start);
            return cachedProducts;
        }

        log.info("CACHE MISS for {} → Fetching from DB...", CACHE_KEY);
        List<ProductResponse> products = productRepository.findAll()
                .stream()
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getSkuCode(),
                        product.getPrice()
                ))
                .toList();

        redisTemplate.opsForValue().set(CACHE_KEY, products);

        long end = System.currentTimeMillis();
        log.info("CACHE MISS for {} (Time: {} ms)", CACHE_KEY, end - start);

        return products;
    }
}

