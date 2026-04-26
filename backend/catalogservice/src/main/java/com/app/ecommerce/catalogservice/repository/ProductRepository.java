package com.app.ecommerce.catalogservice.repository;

import com.app.ecommerce.catalogservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {

    Page<Product> findByCategoryCategoryNameOrderByPriceAsc(String categoryName, Pageable pageDetails);

    Page<Product> findByProductNameContainingIgnoreCase(String keyword, Pageable pageDetails);

    Page<Product> findBySellerId(Long sellerId, Pageable pageDetails);
}