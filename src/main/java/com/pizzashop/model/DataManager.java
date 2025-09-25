package com.pizzashop.model;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {
    private static DataManager instance;
    private List<Item> items;
    private List<Member> members;
    private List<Order> orders;
    private int memberIdCounter = 1;

    private DataManager() {
        items = new ArrayList<>();
        members = new ArrayList<>();
        orders = new ArrayList<>();
        initializeData();
    }

    // Singleton pattern
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private void initializeData() {
        // Initialize items
        items.add(new Item("P001", "Margherita Pizza", 1000.0, "Pizza", "Classic tomato and mozzarella"));
        items.add(new Item("P002", "Pepperoni Pizza", 359.0, "Pizza", "Pepperoni with mozzarella cheese"));
        items.add(new Item("P003", "Hawaiian Pizza", 379.0, "Pizza", "Ham and pineapple"));
        items.add(new Item("P004","พิซซ่าเรดฮาวายเอี้ยน",128.0,"Pizza","nige"));
        items.add(new Item("D001", "Coke", 45.0, "Drink", "Coca Cola 330ml"));
        items.add(new Item("D002", "Orange Juice", 55.0, "Drink", "Fresh orange juice"));

        // Initialize members - แก้ไขการเรียก constructor
        members.add(new Member("M001", "ปาณัสม์ บุญเลา", "0996061879",
                LocalDate.of(1990, 5, 15), LocalDate.of(2025, 12, 31)));

    }

    // Get methods
    public List<Item> getItems() { return items; }
    public List<Member> getMembers() { return members; }
    public List<Order> getOrders() { return orders; }

    // Get all items
    public List<Item> getAllItems() {
        return new ArrayList<>(items);
    }

    // Get all categories
    public List<String> getAllCategories() {
        return items.stream()
                .map(Item::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // Get items by category
    public List<Item> getItemsByCategory(String category) {
        return items.stream()
                .filter(item -> item.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    // Get active members only - แก้ไข method reference
    public List<Member> getActiveMembers() {
        return members.stream()
                .filter(member -> member.isActive())  // แก้ไขจาก method reference
                .collect(Collectors.toList());
    }

    // Find member by ID
    public Member findMemberById(String memberId) {
        return members.stream()
                .filter(member -> member.getMemberId().equals(memberId))
                .findFirst()
                .orElse(null);
    }

    public Optional<Member> findMemberByPhone(String phone) {
        return members.stream()
                .filter(member -> member.getPhone().equals(phone))
                .findFirst();
    }

    // Add new member - แก้ไขให้ return Member
    public Member addMember(String name, String phone, LocalDate birthDate, LocalDate joinDate) {
        String memberId = generateMemberId();
        LocalDate expireDate = joinDate.plusYears(1).minusDays(1);
        Member newMember = new Member(memberId, name, phone, birthDate, expireDate);
        members.add(newMember);
        return newMember;
    }

    // Generate member ID
    private String generateMemberId() {
        return String.format("M%04d", memberIdCounter++);
    }

    // Renew membership
    public void renewMembership(Member member) {
        LocalDate newExpireDate = member.getExpireDate().plusYears(1);
        member.setExpireDate(newExpireDate);
        member.setActive(true);
    }

    // Order management
    public void addOrder(Order order) {
        orders.add(order);
    }

    // Save order
    public void saveOrder(Order order) {
        if (!orders.contains(order)) {
            orders.add(order);
        }
    }

    public Order findOrderById(String orderId) {
        return orders.stream()
                .filter(order -> order.getOrderId().equals(orderId))
                .findFirst()
                .orElse(null);
    }

    // Create new order - แก้ไข method name และ constructor
    public Order createOrder(boolean dineIn) {
        String orderId = generateOrderId();
        return new Order(orderId, null, dineIn);  // ใช้ constructor ที่มี parameters
    }

    private String generateOrderId() {
        return "ORD" + String.format("%06d", orders.size() + 1);
    }

    // Remove order
    public boolean removeOrder(String orderId) {
        return orders.removeIf(o -> o.getOrderId().equals(orderId));
    }

    public void removeOrder(Order order) {
        orders.remove(order);
    }

    // Get orders by member
    public List<Order> getOrdersByMember(Member member) {
        if (member == null) return new ArrayList<>();
        return orders.stream()
                .filter(order -> order.getMember() != null &&
                        order.getMember().getMemberId().equals(member.getMemberId()))
                .collect(Collectors.toList());
    }

    // Get today's orders
    public List<Order> getTodaysOrders() {
        LocalDate today = LocalDate.now();
        return orders.stream()
                .filter(order -> order.getOrderTime().toLocalDate().equals(today))
                .collect(Collectors.toList());
    }

    // Get total sales - แก้ไข method reference
    public double getTotalSales() {
        return orders.stream()
                .mapToDouble(order -> order.getTotalPrice())  // แก้ไขจาก method reference
                .sum();
    }

    // Get sales by date range
    public double getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        return orders.stream()
                .filter(order -> {
                    LocalDate orderDate = order.getOrderTime().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .mapToDouble(order -> order.getTotalPrice())
                .sum();
    }

    // Get expired members - แก้ไข method reference
    public List<Member> getExpiredMembers() {
        return members.stream()
                .filter(member -> !member.isActive())  // แก้ไขจาก method reference
                .collect(Collectors.toList());
    }

    // Find item by ID
    public Item findItemById(String itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    // Get popular items (top 5)
    public List<Item> getPopularItems() {
        Map<String, Integer> itemCount = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem orderItem : order.getOrderItems()) {
                String itemId = orderItem.getItem().getId();
                itemCount.put(itemId, itemCount.getOrDefault(itemId, 0) + orderItem.getQuantity());
            }
        }

        return itemCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> findItemById(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}