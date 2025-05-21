package com.ngxquang.microservices.product.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngxquang.microservices.product.dto.ProductRequest;
import com.ngxquang.microservices.product.dto.ProductResponse;
import com.ngxquang.microservices.product.model.Product;
import com.ngxquang.microservices.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
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
//    public List<ProductResponse> getAllProduct() {
//        return productRepository.findAll()
//                .stream()
//                .map(product -> new ProductResponse(product.getId(), product.getName(), product.getDescription(),
//                        product.getSkuCode(),
//                        product.getPrice()))
//                .toList();
//    }
public List<ProductResponse> getAllProduct() {
    long start = System.currentTimeMillis();

    Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
    if (cached != null) {
        try {
            List<ProductResponse> cachedProducts = objectMapper.convertValue(
                    cached, new TypeReference<List<ProductResponse>>() {}
            );
            long end = System.currentTimeMillis();
            log.info("CACHE HIT for {} (Time: {} ms)", CACHE_KEY, end - start);
            return cachedProducts;
        } catch (Exception e) {
            log.error("Error converting cache value: ", e);
            // Fallback to DB
        }
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

