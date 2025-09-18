package com.jira.jira.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    @Value("${server.reactive.session.cookie.secure}")
    private boolean secureCookie;

    @Value("${server.reactive.session.cookie.same-site}")
    private String sameSiteCookie;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie); // HTTPS only in production
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshTokenExpiration / 1000)); // Convert to seconds
        cookie.setAttribute("SameSite", sameSiteCookie); // CSRF protection

        response.addCookie(cookie);
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Delete cookie
        cookie.setAttribute("SameSite", sameSiteCookie);

        response.addCookie(cookie);
    }
}
