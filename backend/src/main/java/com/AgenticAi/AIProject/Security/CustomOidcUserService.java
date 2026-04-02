package com.AgenticAi.AIProject.Security;



import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.AgenticAi.AIProject.Custom.CustomUserDetails;
import com.AgenticAi.AIProject.Entity.User;
import com.AgenticAi.AIProject.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService
        implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {

        OidcUserService delegate = new OidcUserService();
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String picture = (String) oidcUser.getClaims().get("picture");
        String providerId = oidcUser.getSubject();

        User user = userRepository
                .findByEmail(email)
                .map(existingUser -> {

                    // If user exists but was LOCAL before,
                    // link Google provider
                    if (!"GOOGLE".equals(existingUser.getProvider())) {
                        existingUser.setProvider("GOOGLE");
                        existingUser.setProviderId(providerId);
                        existingUser.setProfileImage(picture);
                        userRepository.save(existingUser);
                    }

                    return existingUser;
                })
                .orElseGet(() -> {

                    // New user → create fresh
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setDisplayName(name);
                    newUser.setProfileImage(picture);
                    newUser.setProvider("GOOGLE");
                    newUser.setProviderId(providerId);
                    newUser.setPassword(null);

                    return userRepository.save(newUser);
                });
      

        return new CustomUserDetails(
                user,
                oidcUser.getClaims(),
                oidcUser.getIdToken()
        );
    }
}