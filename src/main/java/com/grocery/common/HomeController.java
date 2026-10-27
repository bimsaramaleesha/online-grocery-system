package com.grocery.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** The web address "/" simply opens the shop (the storefront lives in the shop module). */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/shop";
    }
}
