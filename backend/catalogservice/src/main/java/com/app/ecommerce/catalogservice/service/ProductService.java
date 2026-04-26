package com.app.ecommerce.catalogservice.service;

import com.app.ecommerce.catalogservice.exceptions.APIException;
import com.app.ecommerce.catalogservice.exceptions.ResourceNotFoundException;
import com.app.ecommerce.catalogservice.model.Category;
import com.app.ecommerce.catalogservice.model.Product;
import com.app.ecommerce.catalogservice.payload.ExternalServiceCartResponse;
import com.app.ecommerce.catalogservice.payload.ExternalServiceUserResponse;
import com.app.ecommerce.catalogservice.payload.ProductDTO;
import com.app.ecommerce.catalogservice.payload.ProductResponse;
import com.app.ecommerce.catalogservice.repository.CategoryRepository;
import com.app.ecommerce.catalogservice.repository.ProductRepository;
import com.app.ecommerce.catalogservice.util.AuthUtil;
import com.app.ecommerce.catalogservice.util.CartUtil;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private CartUtil cartUtil;

    @Value("${project.image}")
    private String path;

    @Value("${image.base.url}")
    private String imageBaseUrl;

    public ProductDTO addProduct(String categoryId, ProductDTO productDTO) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        Product product = modelMapper.map(productDTO, Product.class);

        product.setImage("default.png");
        product.setCategory(category);
        product.setSellerId(authUtil.loggedInUserId());

        double specialPrice = product.getPrice()
                - ((product.getDiscount() * 0.01) * product.getPrice());

        product.setSpecialPrice(specialPrice);

        Product savedProduct = productRepository.save(product);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize,
                                          String sortBy, String sortOrder,
                                          String keyword, String category) {

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> pageProducts;

        if (keyword != null && !keyword.isEmpty() &&
                category != null && !category.isEmpty()) {

            pageProducts = productRepository
                    .findByProductNameContainingIgnoreCaseAndCategoryCategoryName(
                            keyword, category, pageable);

        } else if (keyword != null && !keyword.isEmpty()) {

            pageProducts = productRepository
                    .findByProductNameContainingIgnoreCase(keyword, pageable);

        } else if (category != null && !category.isEmpty()) {

            pageProducts = productRepository
                    .findByCategoryCategoryNameOrderByPriceAsc(category, pageable);

        } else {

            pageProducts = productRepository.findAll(pageable);
        }

        return mapToProductResponse(pageProducts);
    }

    public ProductResponse getAllProductsForAdmin(Integer pageNumber, Integer pageSize,
                                                  String sortBy, String sortOrder) {

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> pageProducts = productRepository.findAll(pageable);

        return mapToProductResponse(pageProducts);
    }

    public ProductResponse getAllProductsForSeller(Integer pageNumber, Integer pageSize,
                                                    String sortBy, String sortOrder) {

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        ExternalServiceUserResponse user = authUtil.loggedInUser();

        Page<Product> pageProducts =
                productRepository.findBySellerId(user.getUserId(), pageable);

        return mapToProductResponse(pageProducts);
    }

    public ProductResponse searchByCategory(String categoryId,
                                            Integer pageNumber, Integer pageSize,
                                            String sortBy, String sortOrder) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> pageProducts =
                productRepository.findByCategoryCategoryName(
                        category.getCategoryName(), pageable);

        if (pageProducts.isEmpty()) {
            throw new APIException(category.getCategoryName() + " category has no products");
        }

        return mapToProductResponse(pageProducts);
    }

    public ProductResponse searchProductByKeyword(String keyword,
                                                   Integer pageNumber, Integer pageSize,
                                                   String sortBy, String sortOrder) {

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> pageProducts =
                productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);

        if (pageProducts.isEmpty()) {
            throw new APIException("Products not found with keyword: " + keyword);
        }

        return mapToProductResponse(pageProducts);
    }


    public ProductDTO updateProduct(String productId, ProductDTO productDTO) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", "productId", productId));

        product.setProductName(productDTO.getProductName());
        product.setDescription(productDTO.getDescription());
        product.setQuantity(productDTO.getQuantity());
        product.setDiscount(productDTO.getDiscount());
        product.setPrice(productDTO.getPrice());

        double specialPrice = product.getPrice()
                - ((product.getDiscount() * 0.01) * product.getPrice());

        product.setSpecialPrice(specialPrice);

        Product saved = productRepository.save(product);

        // update cart service
        List<ExternalServiceCartResponse> carts =
                cartUtil.findCartsByProductId(productId);

        carts.forEach(cart ->
                cartUtil.updateProductInCart(cart.getCartId(), productId));

        return modelMapper.map(saved, ProductDTO.class);
    }

    public ProductDTO deleteProduct(String productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", "productId", productId));

        List<ExternalServiceCartResponse> carts =
                cartUtil.findCartsByProductId(productId);

        carts.forEach(cart ->
                cartUtil.deleteProductFromCart(cart.getCartId(), productId));

        productRepository.delete(product);

        return modelMapper.map(product, ProductDTO.class);
    }

    public ProductDTO updateProductImage(String productId, MultipartFile image) throws IOException {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", "productId", productId));

        String fileName = fileService.uploadImage(path, image);

        product.setImage(fileName);

        Product updated = productRepository.save(product);

        return modelMapper.map(updated, ProductDTO.class);
    }

    public ProductDTO searchByProductId(String productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", "productId", productId));

        return modelMapper.map(product, ProductDTO.class);
    }

    public Long getProductCount() {
        return productRepository.count();
    }

    // ---------------- MAPPER ----------------
    private ProductResponse mapToProductResponse(Page<Product> pageProducts) {

        List<ProductDTO> productDTOS = pageProducts.getContent()
                .stream()
                .map(product -> {
                    ProductDTO dto = modelMapper.map(product, ProductDTO.class);
                    dto.setImage(constructImageUrl(product.getImage()));
                    return dto;
                })
                .toList();

        ProductResponse response = new ProductResponse();
        response.setContent(productDTOS);
        response.setPageNumber(pageProducts.getNumber());
        response.setPageSize(pageProducts.getSize());
        response.setTotalElements(pageProducts.getTotalElements());
        response.setTotalPages(pageProducts.getTotalPages());
        response.setLastPage(pageProducts.isLast());

        return response;
    }

    private String constructImageUrl(String imageName) {
        return imageBaseUrl.endsWith("/")
                ? imageBaseUrl + imageName
                : imageBaseUrl + "/" + imageName;
    }
}
