package com.grocery.order.model;

import com.grocery.common.FileHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a completed or placed customer grocery order.
 * File format:
 * orderId|userId|itemsSummary|subtotal|discountAmount|netAmount|status|orderDate|shippingAddress|contactPhone|paymentMethod|promoCode
 */
public class Order {

    private String orderId;
    private String userId;
    private List<OrderItem> items = new ArrayList<>();
    private double subtotal;
    private double discountAmount;
    private double netAmount;
    private OrderStatus status = OrderStatus.PENDING;
    private String orderDate;
    private String shippingAddress;
    private String contactPhone;
    private String paymentMethod;
    private String promoCode;

    public Order() {
    }

    public Order(String orderId, String userId, List<OrderItem> items, double subtotal,
                 double discountAmount, double netAmount, OrderStatus status, String orderDate,
                 String shippingAddress, String contactPhone, String paymentMethod, String promoCode) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items != null ? items : new ArrayList<>();
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.netAmount = netAmount;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.orderDate = orderDate;
        this.shippingAddress = shippingAddress;
        this.contactPhone = contactPhone;
        this.paymentMethod = paymentMethod;
        this.promoCode = promoCode;
    }

    /** Serializes the Order to a pipe-delimited text line for orders.txt */
    public String toRecordLine() {
        String itemsSummary = items.stream()
                .map(OrderItem::toToken)
                .collect(Collectors.joining(","));

        return String.format("%s|%s|%s|%.2f|%.2f|%.2f|%s|%s|%s|%s|%s|%s",
                FileHandler.clean(orderId),
                FileHandler.clean(userId),
                itemsSummary,
                subtotal,
                discountAmount,
                netAmount,
                status.name(),
                FileHandler.clean(orderDate),
                FileHandler.clean(shippingAddress),
                FileHandler.clean(contactPhone),
                FileHandler.clean(paymentMethod),
                FileHandler.clean(promoCode));
    }

    /** Parses a record line from orders.txt */
    public static Order fromRecordLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length < 8) {
            return null;
        }

        String oId = parts[0].trim();
        String uId = parts[1].trim();

        List<OrderItem> itemList = new ArrayList<>();
        String rawItems = parts[2].trim();
        if (!rawItems.isBlank()) {
            for (String token : rawItems.split(",")) {
                OrderItem item = OrderItem.fromToken(token);
                if (item != null) {
                    itemList.add(item);
                }
            }
        }

        double sub = parseDouble(parts[3], 0.0);
        double disc = parseDouble(parts[4], 0.0);
        double net = parseDouble(parts[5], 0.0);
        OrderStatus st = OrderStatus.fromString(parts[6]);
        String date = parts[7].trim();
        String addr = parts.length > 8 ? parts[8].trim() : "";
        String phone = parts.length > 9 ? parts[9].trim() : "";
        String pay = parts.length > 10 ? parts[10].trim() : "Cash on Delivery";
        String promo = parts.length > 11 ? parts[11].trim() : "";

        return new Order(oId, uId, itemList, sub, disc, net, st, date, addr, phone, pay, promo);
    }

    private static double parseDouble(String str, double def) {
        try {
            return Double.parseDouble(str.trim());
        } catch (Exception ignored) {
            return def;
        }
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(double netAmount) {
        this.netAmount = netAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    /**
     * Builder Pattern: Provides an entry point for fluent Order object creation.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder Pattern: Creational design pattern resolving telescoping constructor issues.
     */
    public static class Builder {
        private String orderId;
        private String userId;
        private List<OrderItem> items = new ArrayList<>();
        private double subtotal;
        private double discountAmount;
        private double netAmount;
        private OrderStatus status = OrderStatus.PENDING;
        private String orderDate;
        private String shippingAddress;
        private String contactPhone;
        private String paymentMethod = "Cash on Delivery";
        private String promoCode = "";

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = items != null ? items : new ArrayList<>();
            return this;
        }

        public Builder addItem(OrderItem item) {
            if (this.items == null) {
                this.items = new ArrayList<>();
            }
            if (item != null) {
                this.items.add(item);
            }
            return this;
        }

        public Builder subtotal(double subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        public Builder discountAmount(double discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }

        public Builder netAmount(double netAmount) {
            this.netAmount = netAmount;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status != null ? status : OrderStatus.PENDING;
            return this;
        }

        public Builder orderDate(String orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Builder shippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
            return this;
        }

        public Builder contactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
            return this;
        }

        public Builder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder promoCode(String promoCode) {
            this.promoCode = promoCode;
            return this;
        }

        public Order build() {
            return new Order(orderId, userId, items, subtotal, discountAmount,
                    netAmount, status, orderDate, shippingAddress, contactPhone,
                    paymentMethod, promoCode);
        }
    }
}
