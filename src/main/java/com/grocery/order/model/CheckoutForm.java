package com.grocery.order.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Form backing object for submitting order checkout information.
 */
public class CheckoutForm {

    @NotBlank(message = "Recipient full name is required")
    private String fullName;

    @NotBlank(message = "Contact phone number is required")
    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Please enter a valid phone number")
    private String contactPhone;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private String deliveryNotes;

    @NotBlank(message = "Please select a payment method")
    private String paymentMethod = "Cash on Delivery";

    private String promoCode;

    public CheckoutForm() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
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
}
