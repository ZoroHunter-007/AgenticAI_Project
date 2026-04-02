package com.AgenticAi.AIProject.Custom;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.AgenticAi.AIProject.Entity.User;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomUserDetails implements UserDetails, OidcUser {

    private final User user;
    private final Map<String, Object> attributes;
    private final OidcIdToken idToken;

    // Manual login constructor
    public CustomUserDetails(User user) {
        this.user = user;
        this.attributes = null;
        this.idToken = null;
    }

    // OIDC login constructor
    public CustomUserDetails(User user,
                             Map<String, Object> attributes,
                             OidcIdToken idToken) {
        this.user = user;
        this.attributes = attributes;
        this.idToken = idToken;
    }

    // ========== UserDetails ==========

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    // ========== OidcUser ==========

    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return new OidcUserInfo(attributes);
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

    @Override
    public String getName() {
        return user.getEmail();
    }
}