package com.app.ecommerce.catalogservice.controller;

import com.app.ecommerce.catalogservice.config.AppConstants;
import com.app.ecommerce.catalogservice.payload.CategoryDTO;
import com.app.ecommerce.catalogservice.payload.CategoryResponse;
import com.app.ecommerce.catalogservice.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_CATEGORIES_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortOrder
    ) {
        return ResponseEntity.ok(
                categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder)
        );
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        return new ResponseEntity<>(
                categoryService.createCategory(categoryDTO),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable String categoryId) {
        return ResponseEntity.ok(
                categoryService.deleteCategory(categoryId)
        );
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @RequestBody CategoryDTO categoryDTO,
            @PathVariable String categoryId) {

        return ResponseEntity.ok(
                categoryService.updateCategory(categoryDTO, categoryId)
        );
    }
}
