package ru.dashboardbattle.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "top_n_entries")
public class TopNEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private TopNReport report;

    @Column(name = "employee_name", length = 255)
    private String employeeName;

    @Column(name = "employee_id_ms", length = 100)
    private String employeeIdMs;

    @Column(precision = 15, scale = 2)
    private BigDecimal revenue;

    @Column(precision = 15, scale = 2)
    private BigDecimal margin;

    @Column(name = "favorite_product", length = 255)
    private String favoriteProduct;

    private Integer rank;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TopNReport getReport() {
        return report;
    }

    public void setReport(TopNReport report) {
        this.report = report;
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
