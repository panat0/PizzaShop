// OrderItem.java
package com.pizzashop.model;

public class OrderItem {
    private Item item;
    private int quantity;
    private double total;

    // Constructors
    public OrderItem() {}

    public OrderItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
        this.total = item.getPrice() * quantity;
    }

    // Getters and Setters
    public Item getItem() { return item; }
    public void setItem(Item item) {
        this.item = item;
        calculateTotal();
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotal();
    }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    private void calculateTotal() {
        if (item != null) {
            this.total = item.getPrice() * quantity;
        }
    }

    @Override
    public String toString() {
        return item.getName() + " x" + quantity + " = ฿" + total;
    }
}