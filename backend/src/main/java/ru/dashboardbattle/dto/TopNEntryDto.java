package ru.dashboardbattle.dto;

import java.math.BigDecimal;

public class TopNEntryDto {

    private Long id;
    private String employeeName;
    private String employeeIdMs;
    private BigDecimal revenue;
    private BigDecimal margin;
    private String favoriteProduct;
    private Integer rank;

    public TopNEntryDto() {
    }

    public TopNEntryDto(String employeeName, String employeeIdMs,
                        BigDecimal revenue, BigDecimal margin,
                        String favoriteProduct, Integer rank) {
        this.employeeName = employeeName;
        this.employeeIdMs = employeeIdMs;
        this.revenue = revenue;
        this.margin = margin;
        this.favoriteProduct = favoriteProduct;
        this.rank = rank;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeIdMs() {
        return employeeIdMs;
    }

    public void setEmployeeIdMs(String employeeIdMs) {
        this.employeeIdMs = employeeIdMs;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }

    public String getFavoriteProduct() {
        return favoriteProduct;
    }

    public void setFavoriteProduct(String favoriteProduct) {
        this.favoriteProduct = favoriteProduct;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
