package com.grocery.user;

import com.grocery.common.FileHandler;
import com.grocery.user.dto.UserForm;
import com.grocery.user.model.Admin;
import com.grocery.user.model.Customer;
import com.grocery.user.model.User;
import com.grocery.user.repository.FileUserRepository;
import com.grocery.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserModuleTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private UserService isolatedService;
    private String testFileName;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        testFileName = "users-test-" + System.nanoTime() + ".txt";
        isolatedService = new UserService(new FileUserRepository(testFileName));
        isolatedService.seedDefaults();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Path.of("data", testFileName));
    }

    @Test
    void seedDefaultsCreatesAdminAndCustomer() {
        assertEquals(2, isolatedService.summary().total());
        assertEquals(1, isolatedService.summary().admins());
        assertEquals(1, isolatedService.summary().customers());

        User admin = isolatedService.authenticate("admin@grocery.lk", "Admin@123");
        assertTrue(admin.canManageUsers());
        assertEquals("/admin", admin.getHomePath());

        User customer = isolatedService.authenticate("customer@grocery.lk", "Customer@123");
        assertEquals(Customer.TYPE, customer.getRole());
        assertEquals("/shop", customer.getHomePath());
    }

    @Test
    void newCustomerRegistrationExistingProfileUpdateAndAdminManagementWork() {
        // 1. New customer registers without an account
        UserForm reg = new UserForm();
        reg.setName("Nimal Perera");
        reg.setEmail("nimal@example.com");
        reg.setPhone("0779876543");
        reg.setAddress("No. 45, Kandy Road");
        reg.setPassword("Secret1234");
        reg.setConfirmPassword("Secret1234");

        User created = isolatedService.register(reg);
        assertNotNull(created.getId());
        assertEquals(Customer.TYPE, created.getRole());

        // 2. Existing customer updates profile and changes password
        UserForm profileForm = UserForm.from(created);
        profileForm.setName("Nimal S. Perera");
        profileForm.setAddress("No. 99, Galle Road");
        User updated = isolatedService.updateProfile(created.getId(), profileForm);
        assertEquals("Nimal S. Perera", updated.getName());

        UserForm pwForm = new UserForm();
        pwForm.setCurrentPassword("Secret1234");
        pwForm.setNewPassword("NewSecret99");
        pwForm.setConfirmPassword("NewSecret99");
        isolatedService.changePassword(created.getId(), pwForm);

        assertNotNull(isolatedService.authenticate("nimal@example.com", "NewSecret99"));

        // 3. Admin creates another admin and deletes customer
        UserForm adminForm = new UserForm();
        adminForm.setType(Admin.TYPE);
        adminForm.setName("Second Admin");
        adminForm.setEmail("admin2@grocery.lk");
        adminForm.setDepartment("Logistics");
        adminForm.setPassword("AdminPass99");
        adminForm.setConfirmPassword("AdminPass99");
        User admin2 = isolatedService.createUser(adminForm);
        assertEquals(2, isolatedService.summary().admins());

        isolatedService.deleteUser(created.getId());
        assertEquals(1, isolatedService.summary().customers());

        // Cannot delete the last admin after deleting admin2
        isolatedService.deleteUser(admin2.getId());
        User firstAdmin = isolatedService.authenticate("admin@grocery.lk", "Admin@123");
        assertThrows(IllegalArgumentException.class, () -> isolatedService.deleteUser(firstAdmin.getId()));
    }

    @Test
    void webRoutesForGuestCustomerAndAdminAreProtectedProperly() throws Exception {
        // 1. Guest (new customer without account) can browse shop, product, login, register
        mockMvc.perform(get("/shop")).andExpect(status().isOk());
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/register")).andExpect(status().isOk());

        // Guest visiting /cart or /admin is redirected to /login
        mockMvc.perform(get("/cart")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/admin")).andExpect(status().is3xxRedirection());

        // 2. Existing Customer session can access /cart, /orders, /users/profile, but NOT /admin
        MockHttpSession customerSession = new MockHttpSession();
        customerSession.setAttribute("userId", "U002");
        customerSession.setAttribute("userName", "Demo Customer");
        customerSession.setAttribute("role", "CUSTOMER");

        mockMvc.perform(get("/cart").session(customerSession)).andExpect(status().isOk());
        mockMvc.perform(get("/orders").session(customerSession)).andExpect(status().isOk());
        mockMvc.perform(get("/users/profile").session(customerSession)).andExpect(status().isOk());
        mockMvc.perform(get("/admin").session(customerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));

        // 3. Admin session can access /admin, /users, /inventory, /suppliers, /promos
        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute("userId", "U001");
        adminSession.setAttribute("userName", "Store Admin");
        adminSession.setAttribute("role", "ADMIN");

        mockMvc.perform(get("/admin").session(adminSession)).andExpect(status().isOk());
        mockMvc.perform(get("/users").session(adminSession)).andExpect(status().isOk());
        mockMvc.perform(get("/inventory").session(adminSession)).andExpect(status().isOk());
        mockMvc.perform(get("/suppliers").session(adminSession)).andExpect(status().isOk());
        mockMvc.perform(get("/promos").session(adminSession)).andExpect(status().isOk());
        mockMvc.perform(post("/logout").session(adminSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }
}
