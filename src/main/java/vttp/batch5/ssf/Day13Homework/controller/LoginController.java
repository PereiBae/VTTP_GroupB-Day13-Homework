package vttp.batch5.ssf.Day13Homework.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Random;
import java.util.logging.Logger;

@Controller
@Validated
public class LoginController {

    private static final String USERNAME = "Brandon";
    private static final String PASSWORD = "JustinBieber";
    private final Logger logger = Logger.getLogger(LoginController.class.getName());

    // Handle both "/" and "/login" paths
    @GetMapping({"/", "/login"})
    public String showLoginPage(HttpSession session, Model model) {
        Integer attempts = (Integer) session.getAttribute("loginAttempts");
        logger.info("Session ID: " + session.getId());

        if (attempts == null) {
            attempts = 0;
            session.setAttribute("loginAttempts", attempts); //Initialize attempts to 0
        }
        boolean showCaptcha = attempts != null && attempts >= 2;

        // Log the number of attempts and if CAPTCHA is displayed
        logger.info("Displaying login page. Current login attempts: " + (attempts != null ? attempts : 0));

        // Only add CAPTCHA to model if it should be shown
        if (showCaptcha) {
            logger.info("Captcha displayed");
            model.addAttribute("captcha", session.getAttribute("captcha"));
        }
        model.addAttribute("showCaptcha", showCaptcha); // Tell view whether to show CAPTCHA
        return "login";
    }

    // processLogin Method (updated)
    @PostMapping("/login")
    public String processLogin(
            @RequestParam @NotBlank String username,
            @RequestParam @NotBlank String password,
            @RequestParam(required = false) String captchaInput,
            HttpSession session,
            Model model) {

        // Initialize or update loginAttempts in the session
        Integer attempts = (Integer) session.getAttribute("loginAttempts");
        if (attempts == null) attempts = 0;

        attempts++;
        session.setAttribute("loginAttempts", attempts); // Update attempts in session

        logger.info("Login attempt count: " + attempts);

        boolean isCaptchaRequired = attempts >= 3;
        String captcha = (String) session.getAttribute("captcha");

        // Check username, password, and CAPTCHA (if required)
        if (USERNAME.equals(username) && PASSWORD.equals(password) &&
                (!isCaptchaRequired || (captcha != null && captcha.equalsIgnoreCase(captchaInput)))) {
            session.invalidate();  // Clear session on successful login
            return "redirect:/secret";
        }

        // If max attempts reached, redirect to lockout
        if (attempts >= 3) {
            session.invalidate();
            return "redirect:/locked";
        }

        // Set CAPTCHA in session if this is the third attempt
        if (attempts == 2) {
            captcha = generateCaptcha();
            session.setAttribute("captcha", captcha);
            model.addAttribute("captcha", captcha);
            model.addAttribute("showCaptcha", true);
            logger.info("Captcha generated and displayed for the user: " + captcha);
        } else {
            model.addAttribute("showCaptcha", attempts >= 2);
        }

        model.addAttribute("error", "Invalid login attempt");
        return "login";
    }

    // Method to generate a random CAPTCHA string
    private String generateCaptcha() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder captcha = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            captcha.append(characters.charAt(random.nextInt(characters.length())));
        }
        return captcha.toString();
    }
}
