package com.app.ecommerce.orderservice.service;

import com.app.ecommerce.orderservice.exceptions.APIException;
import com.app.ecommerce.orderservice.exceptions.ResourceNotFoundException;
import com.app.ecommerce.orderservice.model.Cart;
import com.app.ecommerce.orderservice.model.CartItem;
import com.app.ecommerce.orderservice.model.Order;
import com.app.ecommerce.orderservice.model.OrderItem;
import com.app.ecommerce.orderservice.payload.*;
import com.app.ecommerce.orderservice.repository.CartRepository;
import com.app.ecommerce.orderservice.repository.OrderItemRepository;
import com.app.ecommerce.orderservice.repository.OrderRepository;
import com.app.ecommerce.orderservice.util.AddressUtil;
import com.app.ecommerce.orderservice.util.AuthUtil;
import com.app.ecommerce.orderservice.util.CatalogUtil;
import com.app.ecommerce.orderservice.util.PaymentUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.app.ecommerce.orderservice.payload.OrderItemDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService{

    @Autowired
    CartRepository cartRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    CartService cartService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AddressUtil addressUtil;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    CatalogUtil catalogUtil;

    @Autowired
    PaymentUtil paymentUtil;

    @Transactional
    public OrderDTO placeOrder(String emailId, Long addressId,
                               String paymentMethod, String pgName,
                               String pgPaymentId, String pgStatus,
                               String pgResponseMessage) {

        Cart cart = cartRepository.findCartByEmail(emailId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", emailId);
        }

        if (cart.getCartItems().isEmpty()) {
            throw new APIException("Cart is empty");
        }

        AddressDTO address = addressUtil.getAddressById(addressId);

        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("CREATED");
        order.setAddressId(addressId);

        Order savedOrder = orderRepository.save(order);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaymentMethod(paymentMethod);
        paymentDTO.setPgName(pgName);
        paymentDTO.setPgPaymentId(pgPaymentId);
        paymentDTO.setPgStatus(pgStatus);
        paymentDTO.setPgResponseMessage(pgResponseMessage);

        paymentUtil.savePayment(savedOrder.getOrderId(), paymentDTO);

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);

            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);


        cart.getCartItems().forEach(item -> {
            cartService.deleteProductFromCart(cart.getCartId(), item.getProductId());
        });

        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);

        List<OrderItemDTO> orderItemDTOs = orderItems.stream()
                .map(item -> modelMapper.map(item, OrderItemDTO.class))
                .toList();

        orderDTO.setOrderItems(orderItemDTOs);

        orderDTO.setAddressId(addressId);

        return orderDTO;
    }

    public OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Order> pageOrders = orderRepository.findAll(pageDetails);
        List<Order> orders = pageOrders.getContent();
        List<OrderDTO> orderDTOs = orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setContent(orderDTOs);
        orderResponse.setPageNumber(pageOrders.getNumber());
        orderResponse.setPageSize(pageOrders.getSize());
        orderResponse.setTotalElements(pageOrders.getTotalElements());
        orderResponse.setTotalPages(pageOrders.getTotalPages());
        orderResponse.setLastPage(pageOrders.isLast());
        return orderResponse;
    }

    public OrderDTO updateOrder(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order","orderId",orderId));
        order.setOrderStatus(status);
        orderRepository.save(order);
        return modelMapper.map(order, OrderDTO.class);
    }

    public OrderResponse getAllSellerOrders(Integer pageNumber,
                                            Integer pageSize,
                                            String sortBy,
                                            String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Long sellerId = authUtil.loggedInUserId();

        Page<Order> pageOrders = orderRepository.findAll(pageDetails);

        // 🔥 FILTER USING CATALOG SERVICE
        List<Order> sellerOrders = pageOrders.getContent().stream()
                .filter(order -> order.getOrderItems().stream()
                        .anyMatch(item -> {
                            ProductDTO product = catalogUtil.validateProduct(item.getProductId());

                            return product != null
                                    && product.getSellerId() != null
                                    && product.getSellerId().equals(sellerId);
                        }))
                .toList();

        // Convert to DTO
        List<OrderDTO> orderDTOs = sellerOrders.stream()
                .map(order -> {
                    OrderDTO dto = modelMapper.map(order, OrderDTO.class);

                    List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                            .map(item -> modelMapper.map(item, OrderItemDTO.class))
                            .toList();

                    dto.setOrderItems(itemDTOs);
                    return dto;
                })
                .toList();

        OrderResponse response = new OrderResponse();
        response.setContent(orderDTOs);
        response.setPageNumber(pageOrders.getNumber());
        response.setPageSize(pageOrders.getSize());
        response.setTotalElements(pageOrders.getTotalElements());
        response.setTotalPages(pageOrders.getTotalPages());
        response.setLastPage(pageOrders.isLast());

        return response;
    }

    public Long getOrderCount() {
        return orderRepository.count();
    }

    public Double getTotalRevenue() {
        Double revenue = orderRepository.getTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }


}


