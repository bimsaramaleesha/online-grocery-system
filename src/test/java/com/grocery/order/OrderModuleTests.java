package com.grocery.order;

import com.grocery.order.model.CartItem;
import com.grocery.order.model.CheckoutForm;
import com.grocery.order.model.Order;
import com.grocery.order.model.OrderItem;
import com.grocery.order.model.OrderStatus;
import com.grocery.order.repository.OrderRepository;
import com.grocery.order.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderModuleTests {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    private final String testUser = "U999";

    @BeforeEach
    @AfterEach
    void cleanup() {
        orderService.clearCart(testUser);
    }

    @Test
    void testCartItemSerialization() {
        CartItem item = new CartItem("CRT001", "U001", "ITM001", 3, "2026-10-10 12:00");
        String line = item.toRecordLine();
        CartItem parsed = CartItem.fromRecordLine(line);

        assertNotNull(parsed);
        assertEquals("CRT001", parsed.getCartId());
        assertEquals("U001", parsed.getUserId());
        assertEquals("ITM001", parsed.getItemId());
        assertEquals(3, parsed.getQuantity());
    }

    @Test
    void testOrderItemSerialization() {
        OrderItem item = new OrderItem("ITM001", "Fresh Milk", 2, 250.0);
        String token = item.toToken();
        OrderItem parsed = OrderItem.fromToken(token);

        assertNotNull(parsed);
        assertEquals("ITM001", parsed.getItemId());
        assertEquals(2, parsed.getQuantity());
        assertEquals(250.0, parsed.getUnitPrice());
        assertEquals(500.0, parsed.getLineTotal());
    }

    @Test
    void testAddAndCalculateCart() {
        orderService.addToCart(testUser, "ITM001", 2);
        orderService.addToCart(testUser, "ITM002", 1);

        List<CartItem> cartItems = orderService.getCartForUser(testUser);
        assertEquals(2, cartItems.size());

        double subtotal = orderService.calculateSubtotal(cartItems);
        assertTrue(subtotal > 0.0);

        // Deduplication: adding ITM001 again should increment quantity to 3
        orderService.addToCart(testUser, "ITM001", 1);
        List<CartItem> updatedItems = orderService.getCartForUser(testUser);
        assertEquals(2, updatedItems.size());
        assertEquals(3, updatedItems.stream().filter(i -> i.getItemId().equals("ITM001")).findFirst().get().getQuantity());
    }

    @Test
    void testPlaceOrderAndClearCart() {
        orderService.addToCart(testUser, "ITM001", 2);

        CheckoutForm form = new CheckoutForm();
        form.setFullName("Test Customer");
        form.setContactPhone("0770000000");
        form.setShippingAddress("123 Test Street");
        form.setPaymentMethod("Cash on Delivery");
        form.setPromoCode("WELCOME10");

        Order order = orderService.placeOrder(testUser, form);

        assertNotNull(order);
        assertNotNull(order.getOrderId());
        assertTrue(order.getOrderId().startsWith("ORD"));
        assertTrue(order instanceof Order.CashOrder);
        assertFalse(order.requiresCardTerminal());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertTrue(order.getDiscountAmount() > 0); // 10% promo applied
        assertEquals(Math.round((order.getSubtotal() - order.getDiscountAmount()) * 100.0) / 100.0, order.getNetAmount());

        // Cart must be emptied after order placement
        List<CartItem> remainingCart = orderService.getCartForUser(testUser);
        assertTrue(remainingCart.isEmpty());

        // Can cancel pending order
        boolean cancelled = orderService.cancelOrder(order.getOrderId());
        assertTrue(cancelled);

        Optional<Order> cancelledOrder = orderService.getOrderById(order.getOrderId());
        assertTrue(cancelledOrder.isPresent());
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.get().getStatus());

        // Cannot cancel already cancelled order
        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(order.getOrderId()));

        // Cleanup test order
        orderRepository.deleteById(order.getOrderId());
    }

    @Test
    void testOrderBuilderAndPolymorphism() {
        Order builtOrder = Order.builder()
                .orderId("ORD888")
                .userId("U002")
                .subtotal(500.0)
                .discountAmount(50.0)
                .netAmount(450.0)
                .status(OrderStatus.PENDING)
                .orderDate("2026-10-11 12:00")
                .shippingAddress("45 Galle Road")
                .contactPhone("0771112233")
                .paymentMethod("Card Payment")
                .promoCode("WELCOME10")
                .addItem(new OrderItem("ITM001", "Milk", 2, 250.0))
                .build();

        assertNotNull(builtOrder);
        assertTrue(builtOrder instanceof Order.CardOrder);
        assertTrue(builtOrder.requiresCardTerminal());
        assertEquals("ORD888", builtOrder.getOrderId());
        assertEquals("U002", builtOrder.getUserId());
        assertEquals(500.0, builtOrder.getSubtotal());
        assertEquals(450.0, builtOrder.getNetAmount());
        assertEquals(1, builtOrder.getItems().size());
        assertEquals("Milk", builtOrder.getItems().get(0).getItemName());
    }

    @Test
    void testPromoDiscountCalculations() {
        assertEquals(100.0, orderService.calculatePromoDiscount("WELCOME10", 1000.0));
        assertEquals(200.0, orderService.calculatePromoDiscount("SAVE200", 1500.0));
        assertEquals(0.0, orderService.calculatePromoDiscount("SAVE200", 800.0));
        assertEquals(0.0, orderService.calculatePromoDiscount("INVALID_CODE", 1000.0));
    }
}
