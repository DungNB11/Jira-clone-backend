package com.jira.jira.websocket;

import com.jira.jira.dto.response.TokenValidationResponse;
import com.jira.jira.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {

    private final AuthService authService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Get authorization header
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    // Validate JWT token
                    TokenValidationResponse authResponse = authService.validateToken(authHeader);

                    if (!authResponse.isValid() || authResponse.getUser() == null) {
                        throw new RuntimeException("Invalid token");
                    }

                    // Set user principal for this WebSocket session
                    accessor.setUser(new WebSocketUserPrincipal(
                            authResponse.getUser().getId(),
                            authResponse.getUser().getEmail(),
                            authResponse.getUser().getName()
                    ));

                    log.info("WebSocket connection authenticated for user: {}", authResponse.getUser().getEmail());

                } catch (Exception e) {
                    log.error("WebSocket authentication failed: {}", e.getMessage());
                    throw new RuntimeException("Authentication failed");
                }
            } else {
                log.error("WebSocket connection attempt without valid Authorization header");
                throw new RuntimeException("Authentication required");
            }
        }

        return message;
    }

    /**
     * Custom Principal implementation for WebSocket users
     */
    public static class WebSocketUserPrincipal implements Principal {
        private final String userId;
        private final String email;
        private final String displayName;

        public WebSocketUserPrincipal(String userId, String email, String displayName) {
            this.userId = userId;
            this.email = email;
            this.displayName = displayName;
        }

        @Override
        public String getName() {
            return email; // Use email as principal name
        }

        public String getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
