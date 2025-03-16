package com.ngxquang.microservices.product.repository;

import com.ngxquang.microservices.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
