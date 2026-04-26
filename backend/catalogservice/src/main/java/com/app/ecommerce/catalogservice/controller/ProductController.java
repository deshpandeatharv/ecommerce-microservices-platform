package com.app.ecommerce.catalogservice.controller;

import com.app.ecommerce.catalogservice.config.AppConstants;
import com.app.ecommerce.catalogservice.payload.ProductDTO;
import com.app.ecommerce.catalogservice.payload.ProductResponse;
import com.app.ecommerce.catalogservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/admin/category/{categoryId}")
    public ResponseEntity<ProductDTO> addProductAdmin(
            @RequestBody ProductDTO productDTO,
            @PathVariable String categoryId) {

        return new ResponseEntity<>(
                productService.addProduct(categoryId, productDTO),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/seller/category/{categoryId}")
    public ResponseEntity<ProductDTO> addProductSeller(
            @RequestBody ProductDTO productDTO,
            @PathVariable Long categoryId) {

        return new ResponseEntity<>(
                productService.addProduct(categoryId, productDTO),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortOrder
    ) {

        return ResponseEntity.ok(
                productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder, keyword, category)
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ProductResponse> getByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortOrder
    ) {

        return ResponseEntity.ok(
                productService.searchByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder)
        );
    }

    @GetMapping("/seller/products")
    public ResponseEntity<ProductResponse> getAllProductsForSeller(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ){
        ProductResponse productResponse = productService.getAllProductsForSeller(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse,HttpStatus.OK);
    }

    @GetMapping("/admin/products")
    public ResponseEntity<ProductResponse> getAllProductsForAdmin(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ){
        ProductResponse productResponse = productService.getAllProductsForAdmin(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse,HttpStatus.OK);
    }

    @PutMapping("/seller/products/{productId}")
    public ResponseEntity<ProductDTO> updateProductSeller(@Valid @RequestBody ProductDTO productDTO,
                                                          @PathVariable String productId){
        ProductDTO updatedProductDTO = productService.updateProduct(productId, productDTO);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<ProductResponse> search(
            @PathVariable String keyword,
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR) String sortOrder
    ) {

        return ResponseEntity.ok(
                productService.searchProductByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder)
        );
    }

    @PutMapping("/admin/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductDTO productDTO) {

        return ResponseEntity.ok(
                productService.updateProduct(productId, productDTO)
        );
    }

    @PutMapping("/seller/{productId}")
    public ResponseEntity<ProductDTO> updateProductSeller(
            @PathVariable Long productId,
            @RequestBody ProductDTO productDTO) {

        return ResponseEntity.ok(
                productService.updateProduct(productId, productDTO)
        );
    }

    @DeleteMapping("/admin/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable String productId) {
        return ResponseEntity.ok(
                productService.deleteProduct(productId)
        );
    }
    t
    @DeleteMapping("/seller/{productId}")
    public ResponseEntity<ProductDTO> deleteProductSeller(@PathVariable Long productId) {
        return ResponseEntity.ok(
                productService.deleteProduct(productId)
        );
    }

    @PutMapping("/admin/{productId}/image")
    public ResponseEntity<ProductDTO> uploadImage(
            @PathVariable String productId,
            @RequestParam MultipartFile image) throws IOException {

        return ResponseEntity.ok(
                productService.updateProductImage(productId, image)
        );
    }

    @PutMapping("/seller/{productId}/image")
    public ResponseEntity<ProductDTO> uploadImageSeller(
            @PathVariable Long productId,
            @RequestParam MultipartFile image) throws IOException {

        return ResponseEntity.ok(
                productService.updateProductImage(productId, image)
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getById(@PathVariable String productId) {
        return ResponseEntity.ok(
                productService.searchByProductId(productId)
        );
    }

    @GetMapping("/admin/count")
    public ResponseEntity<Long> getProductCount() {
        return ResponseEntity.ok(productService.getProductCount());
    }
}