package vttp.batch5.ssf.Day13Homework.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SecretController {

    @GetMapping("/secret")
    public String showSecretPage() {
        return "secret";
    }

    @GetMapping("/locked")
    public String showLockedPage() {
        return "locked";
    }
}
