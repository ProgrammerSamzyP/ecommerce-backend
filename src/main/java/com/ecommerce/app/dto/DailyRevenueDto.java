package com.ecommerce.app.dto;
import com.ecommerce.app.dto.DailyRevenueDto;

public class DailyRevenueDto {
    private String date;
    private double revenue;

    public DailyRevenueDto(String date, double revenue) {
        this.date = date;
        this.revenue = revenue;
    }

    public String getDate() { return date; }
    public double getRevenue() { return revenue; }
}