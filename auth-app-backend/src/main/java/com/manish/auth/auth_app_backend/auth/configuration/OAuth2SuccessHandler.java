package com.manish.auth.auth_app_backend.auth.configuration;

import com.manish.auth.auth_app_backend.auth.entities.Provider;
import com.manish.auth.auth_app_backend.auth.entities.RefreshToken;
import com.manish.auth.auth_app_backend.auth.entities.Role;
import com.manish.auth.auth_app_backend.auth.entities.User;
import com.manish.auth.auth_app_backend.auth.repositories.RefreshTokenRepository;
import com.manish.auth.auth_app_backend.auth.repositories.RoleRepositories;
import com.manish.auth.auth_app_backend.auth.repositories.UserRepository;
import com.manish.auth.auth_app_backend.auth.services.serviceImpl.CookieService;
import com.manish.auth.auth_app_backend.auth.services.serviceImpl.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepositories roleRepositories;

    @Value("${app.auth.frontend.success-redirect}")
    private String frontEndSuccessUrl;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        logger.info("Authentication Success");
        logger.info(authentication.toString());
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

//        identify user
        String registrationId = "unknown";
        if (authentication instanceof OAuth2AuthenticationToken token) {
            registrationId = token.getAuthorizedClientRegistrationId();
        }
        logger.info("Registration Id: " + registrationId);
        logger.info("User : " + oAuth2User.getAttributes().toString());

        User user;
        switch (registrationId) {
            case "google" -> {
                String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();
                String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("picture", "").toString();
                User newUser = User.builder()
                        .email(email)
                        .name(name)
                        .image(picture)
                        .isEnable(true)
                        .provider(Provider.GOOGLE)
                        .providerId(googleId)
                        .build();

                Role role = roleRepositories.findByName("ROLE_" + AppConstants.GUEST_ROLE).orElse(null);
                if (newUser.getRoles() == null) {
                    newUser.setRoles(new java.util.HashSet<>());
                }
                if (role != null) {
                    newUser.getRoles().add(role);
                }

                user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(newUser));
            }
            case "github" -> {
                String name = oAuth2User.getAttributes().getOrDefault("login", "").toString();
                String githubId = oAuth2User.getAttributes().getOrDefault("id", "").toString();
                String image = oAuth2User.getAttributes().getOrDefault("avatar_url", "").toString();

                String email = (String) oAuth2User.getAttributes().get("email");
                if (email == null) {
                    email = name + "@github.com";
                }

                User newUser = User.builder()
                        .email(email)
                        .name(name)
                        .image(image)
                        .isEnable(true)
                        .provider(Provider.GITHUB)
                        .providerId(githubId)
                        .build();

                Role role = roleRepositories.findByName("ROLE_" + AppConstants.GUEST_ROLE).orElse(null);
                if (newUser.getRoles() == null) {
                    newUser.setRoles(new java.util.HashSet<>());
                }
                if (role != null) {
                    newUser.getRoles().add(role);
                }

                user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(newUser));
            }
            default -> {
                throw new RuntimeException("Invalid registration id");
            }
        }

//        username
//        user email
//        new user create
//        jwt token - redirect to frontend with jwt token

//        create refresh token
        String jti = UUID.randomUUID().toString();
        RefreshToken refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();

        refreshTokenRepository.save(refreshTokenOb);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenOb.getJti());

        cookieService.attachRefreshTokenCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());

//        response.getWriter().write("Login Successful");
        response.sendRedirect(frontEndSuccessUrl);
    }
}
