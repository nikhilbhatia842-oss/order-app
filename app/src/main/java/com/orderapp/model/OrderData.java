package com.orderapp.model;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Order data model that represents the form submission
 */
public class OrderData {
    
    @SerializedName("shopName")
    private String shopName;
    
    @SerializedName("orderDate")
    private String orderDate;
    
    @SerializedName("salesmanName")
    private String salesmanName;
    
    @SerializedName("numberOfBoxes")
    private int numberOfBoxes;
    
    @SerializedName("specification")
    private String specification;
    
    @SerializedName("amountPerBox")
    private double amountPerBox;
    
    @SerializedName("totalAmount")
    private double totalAmount;
    
    @SerializedName("location")
    private String location;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("submissionTime")
    private String submissionTime;

    // Constructor
    public OrderData(String shopName, String orderDate, String salesmanName, 
                     int numberOfBoxes, String specification, double amountPerBox, 
                     String location, String phoneNumber) {
        this.shopName = shopName;
        this.orderDate = orderDate;
        this.salesmanName = salesmanName;
        this.numberOfBoxes = numberOfBoxes;
        this.specification = specification;
        this.amountPerBox = amountPerBox;
        this.totalAmount = numberOfBoxes * amountPerBox;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.submissionTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Getters and Setters
    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getSalesmanName() {
        return salesmanName;
    }

    public void setSalesmanName(String salesmanName) {
        this.salesmanName = salesmanName;
    }

    public int getNumberOfBoxes() {
        return numberOfBoxes;
    }

    public void setNumberOfBoxes(int numberOfBoxes) {
        this.numberOfBoxes = numberOfBoxes;
        updateTotalAmount();
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public double getAmountPerBox() {
        return amountPerBox;
    }

    public void setAmountPerBox(double amountPerBox) {
        this.amountPerBox = amountPerBox;
        updateTotalAmount();
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }

    private void updateTotalAmount() {
        this.totalAmount = numberOfBoxes * amountPerBox;
    }
}
