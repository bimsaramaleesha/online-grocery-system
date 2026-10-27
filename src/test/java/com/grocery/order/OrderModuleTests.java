package com.grocery.order;

import com.grocery.order.model.CartItem;
import com.grocery.order.model.CheckoutForm;
import com.grocery.order.model.Order;
import com.grocery.order.model.OrderItem;
import com.grocery.order.model.OrderStatus;
import com.grocery.order.repository.CartRepository;
import com.grocery.order.repository.OrderRepository;
import com.grocery.order.service.CartService;
import com.grocery.order.service.ItemCatalogService;
import com.grocery.order.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderModuleTests {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemCatalogService itemCatalogService;

    private final String testUser = "U999";

    @BeforeEach
    @AfterEach
    void cleanup() {
        cartService.clearCart(testUser);
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
        cartService.addToCart(testUser, "ITM001", 2);
        cartService.addToCart(testUser, "ITM002", 1);

        List<CartItem> cartItems = cartService.getCartForUser(testUser);
        assertEquals(2, cartItems.size());

        double subtotal = cartService.calculateSubtotal(cartItems);
        assertTrue(subtotal > 0.0);

        // Deduplication: adding ITM001 again should increment quantity to 3
        cartService.addToCart(testUser, "ITM001", 1);
        List<CartItem> updatedItems = cartService.getCartForUser(testUser);
        assertEquals(2, updatedItems.size());
        assertEquals(3, updatedItems.stream().filter(i -> i.getItemId().equals("ITM001")).findFirst().get().getQuantity());
    }

    @Test
    void testPlaceOrderAndClearCart() {
        cartService.addToCart(testUser, "ITM001", 2);

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
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertTrue(order.getDiscountAmount() > 0); // 10% promo applied
        assertEquals(Math.round((order.getSubtotal() - order.getDiscountAmount()) * 100.0) / 100.0, order.getNetAmount());

        // Cart must be emptied after order placement
        List<CartItem> remainingCart = cartService.getCartForUser(testUser);
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
    void testOrderBuilderPattern() {
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
                .paymentMethod("Cash on Delivery")
                .promoCode("WELCOME10")
                .addItem(new OrderItem("ITM001", "Milk", 2, 250.0))
                .build();

        assertNotNull(builtOrder);
        assertEquals("ORD888", builtOrder.getOrderId());
        assertEquals("U002", builtOrder.getUserId());
        assertEquals(500.0, builtOrder.getSubtotal());
        assertEquals(450.0, builtOrder.getNetAmount());
        assertEquals(1, builtOrder.getItems().size());
        assertEquals("Milk", builtOrder.getItems().get(0).getItemName());
    }

    @Test
    void testDiscountStrategyPattern() {
        com.grocery.order.strategy.discount.DiscountStrategyFactory factory =
                new com.grocery.order.strategy.discount.DiscountStrategyFactory();

        // 10% percentage discount
        com.grocery.order.strategy.discount.DiscountStrategy welcome10 = factory.getStrategy("WELCOME10");
        assertTrue(welcome10 instanceof com.grocery.order.strategy.discount.PercentageDiscountStrategy);
        assertEquals(100.0, welcome10.calculateDiscount(1000.0));

        // Fixed Rs. 200 discount if subtotal >= 1000
        com.grocery.order.strategy.discount.DiscountStrategy save200 = factory.getStrategy("SAVE200");
        assertTrue(save200 instanceof com.grocery.order.strategy.discount.FixedAmountDiscountStrategy);
        assertEquals(200.0, save200.calculateDiscount(1500.0));
        assertEquals(0.0, save200.calculateDiscount(800.0)); // under threshold

        // Unknown code -> NoDiscountStrategy
        com.grocery.order.strategy.discount.DiscountStrategy unknown = factory.getStrategy("INVALID_CODE");
        assertTrue(unknown instanceof com.grocery.order.strategy.discount.NoDiscountStrategy);
        assertEquals(0.0, unknown.calculateDiscount(1000.0));
    }

    @Test
    void testPaymentStrategyPattern() {
        com.grocery.order.strategy.payment.PaymentStrategyFactory factory =
                new com.grocery.order.strategy.payment.PaymentStrategyFactory();

        com.grocery.order.strategy.payment.PaymentStrategy cod = factory.getStrategy("Cash on Delivery");
        assertFalse(cod.requiresCardTerminal());
        assertEquals("Cash on Delivery", cod.getMethodName());

        com.grocery.order.strategy.payment.PaymentStrategy card = factory.getStrategy("Card Payment");
        assertTrue(card.requiresCardTerminal());
        assertEquals("Card Payment", card.getMethodName());
    }
}
