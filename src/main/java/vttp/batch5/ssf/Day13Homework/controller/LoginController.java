package vttp.batch5.ssf.Day13Homework.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vttp.batch5.ssf.Day13Homework.config.Constants;

import java.util.Random;
import java.util.logging.Logger;

@Controller
@Validated
public class LoginController {

    private final Logger logger = Logger.getLogger(LoginController.class.getName());

    @GetMapping({"/", "/login"})
    public String showLoginPage(HttpSession session, Model model) {
        Integer attempts = (Integer) session.getAttribute(Constants.LOGIN_ATTEMPTS);
        if (attempts == null) {
            attempts = 0;
            session.setAttribute(Constants.LOGIN_ATTEMPTS, attempts);
        }

        boolean showCaptcha = attempts >= Constants.SHOW_CAPTCHA_AFTER_ATTEMPTS;
        logger.info("Displaying login page. Current login attempts: " + attempts);

        if (showCaptcha) {
            logger.info("CAPTCHA is displayed for the user.");
            model.addAttribute("captcha", session.getAttribute(Constants.CAPTCHA));
        }

        model.addAttribute("showCaptcha", showCaptcha);
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam @NotBlank String username,
            @RequestParam @NotBlank String password,
            @RequestParam(required = false) String captchaInput,
            HttpSession session,
            Model model) {

        Integer attempts = (Integer) session.getAttribute(Constants.LOGIN_ATTEMPTS);
        if (attempts == null) attempts = 0;

        attempts++;
        session.setAttribute(Constants.LOGIN_ATTEMPTS, attempts);
        logger.info("Login attempt #" + attempts);

        boolean isCaptchaRequired = attempts >= Constants.MAX_ATTEMPTS;
        String captcha = (String) session.getAttribute(Constants.CAPTCHA);

        if (Constants.USERNAME.equals(username) && Constants.PASSWORD.equals(password) &&
                (!isCaptchaRequired || (captcha != null && captcha.equalsIgnoreCase(captchaInput)))) {
            session.invalidate();
            return "redirect:/secret";
        }

        if (attempts >= Constants.MAX_ATTEMPTS) {
            session.invalidate();
            return "redirect:/locked";
        }

        if (attempts == Constants.SHOW_CAPTCHA_AFTER_ATTEMPTS) {
            captcha = generateCaptcha();
            session.setAttribute(Constants.CAPTCHA, captcha);
            model.addAttribute("captcha", captcha);
            model.addAttribute("showCaptcha", true);
            logger.info("CAPTCHA generated and displayed for the user.");
        } else {
            model.addAttribute("showCaptcha", attempts >= Constants.SHOW_CAPTCHA_AFTER_ATTEMPTS);
        }

        model.addAttribute("error", "Invalid login attempt");
        return "login";
    }

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
