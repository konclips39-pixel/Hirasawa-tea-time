package com.yuihara;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeRedirect {
    @GetMapping("/")
    public String home() {
        // This sends the user from http://localhost:8080 directly to your dashboard
        return "redirect:/dashboard/main";
    }
}