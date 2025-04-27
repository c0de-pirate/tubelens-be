package codepirate.tubelensbe.home.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    public String home(@AuthenticationPrincipal Object principal) {
        if (principal != null) {
            return "Backend API Server is running. User: " + principal;
        }
        return "Backend API Server is running. No authenticated user.";
    }
}