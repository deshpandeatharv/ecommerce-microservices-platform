package com.app.ecommerce.catalogservice.repository;

import com.app.ecommerce.catalogservice.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {

    Category findByCategoryName(String categoryName);
}