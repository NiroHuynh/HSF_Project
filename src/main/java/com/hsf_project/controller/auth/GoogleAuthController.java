package com.hsf_project.controller.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.hsf_project.entity.Role;
import com.hsf_project.entity.User;
import com.hsf_project.repository.auth.RoleRepository;
import com.hsf_project.repository.auth.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/auth/google")
public class GoogleAuthController {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/login")
    public String redirectToGoogle(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute("oauth2State", state);

        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);

        return "redirect:" + authUrl;
    }

    @GetMapping("/callback")
    public String handleCallback(@RequestParam("code") String code,
                                 @RequestParam("state") String state,
                                 HttpSession session) {
        String savedState = (String) session.getAttribute("oauth2State");
        if (savedState == null || !savedState.equals(state)) {
            return "redirect:/?error=invalid_state";
        }
        session.removeAttribute("oauth2State");

        try {
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    clientId,
                    clientSecret,
                    code,
                    redirectUri
            ).execute();

            GoogleIdToken idToken = tokenResponse.parseIdToken();
            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String firstName = (String) payload.get("family_name");
            String lastName = (String) payload.get("given_name");

            if (firstName == null) firstName = "";
            if (lastName == null) lastName = email.split("@")[0];

            User user = findOrCreateGoogleUser(email, firstName, lastName);
            session.setAttribute("ttdn", user);

            String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
            if (redirectUrl != null) {
                session.removeAttribute("redirectAfterLogin");
                return "redirect:" + redirectUrl;
            }
            return "redirect:/home";

        } catch (Exception e) {
            return "redirect:/?error=google_login_failed";
        }
    }

    private User findOrCreateGoogleUser(String email, String firstName, String lastName) {
        Optional<User> existing = userRepository.findByEmailAndIsDeletedFalse(email);
        if (existing.isPresent()) {
            return existing.get();
        }

        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role CUSTOMER not found"));

        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword("GOOGLE_OAUTH_" + UUID.randomUUID());
        user.setRole(customerRole);
        user.setStatus("ACTIVE");
        user.setIsDeleted(false);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}
