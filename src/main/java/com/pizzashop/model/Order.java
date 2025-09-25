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

        for (OrderItem item : orderItems) {
            totalPrice += item.getTotal();
        }

        // Apply member discounts
        if (member != null) {
            // Birthday discount (15%)
            if (member.isBirthday()) {
                double birthdayDiscount = totalPrice * 0.15;
                totalSavings += birthdayDiscount;
            }

            // Wednesday free pizza if sum order > 1000
            if (LocalDateTime.now().getDayOfWeek().getValue() == 3 && totalPrice >= 1000 ) { // Wednesday
                // Find cheapest pizza and make it free
                double cheapestPizza = orderItems.stream()
                        .filter(item -> item.getItem().getName().equalsIgnoreCase("พิซซ่าเรดฮาวายเอี้ยน"))
                        .mapToDouble(item -> item.getItem().getPrice())
                        .min()
                        .orElse(0.0);

                if (cheapestPizza > 0) {
                    totalSavings += cheapestPizza;
                    hasFreeWednesdayPizza = true;
                }
            }
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
        return totalPrice - totalSavings;
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
}