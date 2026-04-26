package com.app.ecommerce.analyticsservice.service;

import com.app.ecommerce.analyticsservice.payload.AnalyticsResponse;
import com.app.ecommerce.analyticsservice.util.CatalogUtil;
import com.app.ecommerce.analyticsservice.util.OrderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService{

    @Autowired
    private CatalogUtil catalogUtil;

    @Autowired
    private OrderUtil orderUtil;


    public AnalyticsResponse getAnalyticsData() {

        AnalyticsResponse response = new AnalyticsResponse();

        Long productCount = catalogUtil.getProductCount();
        Long totalOrders = orderUtil.getOrderCount();
        Double totalRevenue = orderUtil.getTotalRevenue();

        response.setProductCount(String.valueOf(productCount));
        response.setTotalOrders(String.valueOf(totalOrders));
        response.setTotalRevenue(String.valueOf(totalRevenue));

        return response;
    }
}
