// Order.java
package com.pizzashop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private Member member;
    private boolean dineIn;
    private LocalDateTime orderTime;
    private List<OrderItem> orderItems;
    private double totalPrice;
    private double totalSavings;
    private boolean hasFreeWednesdayPizza;

    // Default constructor
    public Order() {
        this.orderItems = new ArrayList<>();
        this.orderTime = LocalDateTime.now();
        this.totalPrice = 0.0;
        this.totalSavings = 0.0;
        this.hasFreeWednesdayPizza = false;
        generateOrderId();
    }

    // Constructor with parameters
    public Order(String orderId, Member member, boolean dineIn) {
        this();
        this.orderId = orderId;
        this.member = member;
        this.dineIn = dineIn;
    }

    // Generate unique order ID
    private void generateOrderId() {
        if (orderId == null) {
            this.orderId = "ORD" + System.currentTimeMillis();
        }
    }

    // Add item to order
    public void addItem(Item item, int quantity) {
        // Check if item already exists in order
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getItem().getId().equals(item.getId())) {
                orderItem.setQuantity(orderItem.getQuantity() + quantity);
                calculateTotals();
                return;
            }
        }
        // Add new item
        orderItems.add(new OrderItem(item, quantity));
        calculateTotals();
    }

    // Calculate totals and apply discounts
    private void calculateTotals() {
        totalPrice = 0.0;
        totalSavings = 0.0;
        hasFreeWednesdayPizza = false;

        // 1. คำนวณราคาพื้นฐานทั้งหมด
        double basePrice = 0.0;
        for (OrderItem item : orderItems) {
            basePrice += item.getTotal();
        }

        // เริ่มต้นด้วยราคาพื้นฐาน
        totalPrice = basePrice;

        // 2. โปรโมชั่นวันพุธ - พิซซ่าฟรี (ใช้ได้ทั้งสมาชิกและไม่เป็นสมาชิก)
        double wednesdayDiscount = applyWednesdayPromotion(basePrice);
        if (wednesdayDiscount > 0) {
            totalSavings += wednesdayDiscount;
            totalPrice -= wednesdayDiscount;
            hasFreeWednesdayPizza = true;
        }

        // 3. ส่วนลดสมาชิก (คำนวณจากราคาหลังหักโปรโมชั่นแล้ว)
        if (member != null) {
            double memberDiscount = applyMemberDiscount(totalPrice);
            if (memberDiscount > 0) {
                totalSavings += memberDiscount;
                totalPrice -= memberDiscount;
            }
        }
    }

    /**
     * ตรวจสอบและใช้โปรโมชั่นวันพุธ
     * @param currentTotal ราคาปัจจุบัน
     * @return จำนวนเงินที่ลดได้
     */
    private double applyWednesdayPromotion(double currentTotal) {
        // เช็ควันพุธ (3) และราคาขั้นต่ำ 1000 บาท
        if (LocalDateTime.now().getDayOfWeek().getValue() != 5 || currentTotal < 1000) {
            return 0.0;
        }

        // หาพิซซ่าเรดฮาวายเอี้ยนในออเดอร์
        return orderItems.stream()
                .filter(item -> item.getItem().getName().equalsIgnoreCase("พิซซ่าเรดฮาวายเอี้ยน"))
                .mapToDouble(item -> item.getItem().getPrice())
                .findFirst() // เอาแค่ตัวแรกที่เจอ (ฟรี 1 ถาดเท่านั้น)
                .orElse(0.0);
    }

    /**
     * คำนวณส่วนลดสมาชิก
     * @param currentTotal ราคาปัจจุบัน (หลังหักโปรโมชั่นอื่นแล้ว)
     * @return จำนวนเงินที่ลดได้
     */
    private double applyMemberDiscount(double currentTotal) {
        if (member == null || currentTotal <= 0) {
            return 0.0;
        }

        // ตรวจสอบส่วนลดวันเกิดก่อน
        if (member.isBirthday()) {
            return currentTotal * 0.15; // 15% วันเกิด
        } else {
            return currentTotal * 0.10; // 10% สมาชิกปกติ
        }
    }

    // Check if order is empty
    public boolean isEmpty() {
        return orderItems.isEmpty();
    }

    // Clear all items
    public void clear() {
        orderItems.clear();
        totalPrice = 0.0;
        totalSavings = 0.0;
        hasFreeWednesdayPizza = false;
    }

    // Get order summary
    public String getOrderSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Order ID: ").append(orderId).append("\n");
        summary.append("Items:\n");

        for (OrderItem item : orderItems) {
            summary.append("- ").append(item.toString()).append("\n");
        }

        summary.append("Total: ฿").append(String.format("%.2f", getTotalPrice()));

        if (totalSavings > 0) {
            summary.append("\nSavings: ฿").append(String.format("%.2f", totalSavings));
        }

        return summary.toString();
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Member getMember() { return member; }
    public void setMember(Member member) {
        this.member = member;
        calculateTotals(); // Recalculate when member changes
    }

    public boolean isDineIn() { return dineIn; }
    public void setDineIn(boolean dineIn) { this.dineIn = dineIn; }

    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
        calculateTotals();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public double getTotalSavings() { return totalSavings; }
    public void setTotalSavings(double totalSavings) { this.totalSavings = totalSavings; }

    public boolean hasFreeWednesdayPizza() { return hasFreeWednesdayPizza; }
    public void setHasFreeWednesdayPizza(boolean hasFreeWednesdayPizza) {
        this.hasFreeWednesdayPizza = hasFreeWednesdayPizza;
    }

    @Override
    public String toString() {
        return "Order " + orderId + " - ฿" + String.format("%.2f", getTotalPrice());
    }

    // เพิ่ม method นี้ใน Order.java หลังจาก addItem method

    // Remove item from order
    public void removeItem(Item item) {
        orderItems.removeIf(orderItem -> orderItem.getItem().getId().equals(item.getId()));
        calculateTotals();
    }

    // หรือถ้าต้องการลบเฉพาะจำนวนที่กำหนด
    public void removeItem(Item item, int quantity) {
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getItem().getId().equals(item.getId())) {
                int newQuantity = orderItem.getQuantity() - quantity;
                if (newQuantity <= 0) {
                    orderItems.remove(orderItem);
                } else {
                    orderItem.setQuantity(newQuantity);
                }
                calculateTotals();
                return;
            }
        }
    }

    public double getOriginalTotalPrice() {
        return orderItems.stream()
                .mapToDouble(item -> item.getItem().getPrice() * item.getQuantity())
                .sum();
    }


}