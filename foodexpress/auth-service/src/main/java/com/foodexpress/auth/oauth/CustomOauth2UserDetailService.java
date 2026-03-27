package com.foodexpress.auth.oauth;

import com.foodexpress.auth.model.entity.User;
import com.foodexpress.auth.repository.UserRepository;
import com.foodexpress.common.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomOauth2UserDetailService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(CustomOauth2UserDetailService.class);
    @Override
    @Transactional
    public CustomOauth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User=super.loadUser(oAuth2UserRequest);
        try {
            return processOauth2User(oAuth2User, oAuth2UserRequest);
        } catch (Exception ex) {
            logger.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException("Error processing OAuth2 user");
        }
    }

    private CustomOauth2User processOauth2User(OAuth2User oAuth2User, OAuth2UserRequest oAuth2UserRequest) {
        Logger logger = LoggerFactory.getLogger(CustomOauth2UserDetailService.class);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String googleId = (String) attributes.get("sub");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        Boolean emailVerified = (Boolean) attributes.get("email_verified");
        logger.info("Processing OAuth2 user: email={}", email);
        Optional<User> userOptional=userRepository.findByEmail(googleId);
        User user;
        if(userOptional.isPresent()) {
            user = userOptional.get();
            user.setEmailVerified(emailVerified);
            user.setName(name);
            user.setProfilePicture(picture);
            userRepository.save(user);
        }else {
            Optional<User> exitsByEmail = userRepository.findByEmail(email);
            if(exitsByEmail.isPresent()) {
                user = exitsByEmail.get();
                user.setEmailVerified(true);
                user.setGoogleId(googleId);
                user.setName(name);
                user.setProfilePicture(picture);
                userRepository.save(user);
            }else{
                user=User.builder()
                        .email(email)
                        .name(name)
                        .profilePicture(picture)
                        .role(Role.ROLE_CUSTOMER)
                        .emailVerified(emailVerified)
                        .enabled(true)
                        .googleId(googleId)
                        .provider("GOOGLE")
                        .build();
                userRepository.save(user);
            }
        }
        return new CustomOauth2User(user,oAuth2User);
    }
}
