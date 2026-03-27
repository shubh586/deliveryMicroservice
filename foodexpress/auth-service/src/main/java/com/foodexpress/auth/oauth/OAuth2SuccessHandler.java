package com.foodexpress.auth.oauth;
import com.foodexpress.auth.model.entity.RefreshToken;
import com.foodexpress.auth.model.entity.User;
import com.foodexpress.auth.repository.RefreshTokenRepository;
import com.foodexpress.auth.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomOauth2User oAuth2User = (CustomOauth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        logger.info("OAuth2 authentication successful for user: {}", user.getEmail());


        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        String refreshTokenString = jwtTokenProvider.generateRefreshToken();


        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenString)
                .expiresAt(jwtTokenProvider.getRefreshTokenExpiry())
                .build();
        refreshTokenRepository.save(refreshToken);


        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("access_token", accessToken)
                .queryParam("refresh_token", refreshTokenString)
                .build().toUriString();

        logger.info("Redirecting to: {}", targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
