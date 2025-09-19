package com.jira.jira.controller;

import com.jira.jira.service.WebSocketService;
import com.jira.jira.websocket.WebSocketAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final WebSocketService webSocketService;

    /**
     * Handle user joining a workspace
     */
    @MessageMapping("/workspace/{workspaceId}/join")
    public void joinWorkspace(@DestinationVariable String workspaceId, Principal principal) {
        if (principal instanceof WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) {
            WebSocketAuthenticationInterceptor.WebSocketUserPrincipal user =
                    (WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) principal;

            log.info("User {} joined workspace {}", user.getEmail(), workspaceId);

            // Send user presence update
            webSocketService.sendUserPresenceUpdate(workspaceId, user.getUserId(), true);
        }
    }

    /**
     * Handle user leaving a workspace
     */
    @MessageMapping("/workspace/{workspaceId}/leave")
    public void leaveWorkspace(@DestinationVariable String workspaceId, Principal principal) {
        if (principal instanceof WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) {
            WebSocketAuthenticationInterceptor.WebSocketUserPrincipal user =
                    (WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) principal;

            log.info("User {} left workspace {}", user.getEmail(), workspaceId);

            // Send user presence update
            webSocketService.sendUserPresenceUpdate(workspaceId, user.getUserId(), false);
        }
    }

    /**
     * Handle subscription to workspace task updates
     */
    @SubscribeMapping("/topic/workspace/{workspaceId}/tasks")
    public void subscribeToWorkspaceTasks(@DestinationVariable String workspaceId, Principal principal) {
        if (principal instanceof WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) {
            WebSocketAuthenticationInterceptor.WebSocketUserPrincipal user =
                    (WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) principal;

            log.info("User {} subscribed to workspace {} task updates", user.getEmail(), workspaceId);
        }
    }

    /**
     * Handle subscription to project task updates
     */
    @SubscribeMapping("/topic/project/{projectId}/tasks")
    public void subscribeToProjectTasks(@DestinationVariable String projectId, Principal principal) {
        if (principal instanceof WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) {
            WebSocketAuthenticationInterceptor.WebSocketUserPrincipal user =
                    (WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) principal;

            log.info("User {} subscribed to project {} task updates", user.getEmail(), projectId);
        }
    }

    /**
     * Handle subscription to kanban updates
     */
    @SubscribeMapping("/topic/workspace/{workspaceId}/kanban")
    public void subscribeToKanbanUpdates(@DestinationVariable String workspaceId, Principal principal) {
        if (principal instanceof WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) {
            WebSocketAuthenticationInterceptor.WebSocketUserPrincipal user =
                    (WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) principal;

            log.info("User {} subscribed to workspace {} kanban updates", user.getEmail(), workspaceId);
        }
    }

    /**
     * Handle subscription to activity feed
     */
    @SubscribeMapping("/topic/workspace/{workspaceId}/activity")
    public void subscribeToActivity(@DestinationVariable String workspaceId, Principal principal) {
        if (principal instanceof WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) {
            WebSocketAuthenticationInterceptor.WebSocketUserPrincipal user =
                    (WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) principal;

            log.info("User {} subscribed to workspace {} activity feed", user.getEmail(), workspaceId);
        }
    }

    /**
     * Handle subscription to user presence updates
     */
    @SubscribeMapping("/topic/workspace/{workspaceId}/presence")
    public void subscribeToPresence(@DestinationVariable String workspaceId, Principal principal) {
        if (principal instanceof WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) {
            WebSocketAuthenticationInterceptor.WebSocketUserPrincipal user =
                    (WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) principal;

            log.info("User {} subscribed to workspace {} presence updates", user.getEmail(), workspaceId);
        }
    }

    /**
     * Handle ping messages for connection health check
     */
    @MessageMapping("/ping")
    public void handlePing(@Payload String message, Principal principal) {
        if (principal instanceof WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) {
            WebSocketAuthenticationInterceptor.WebSocketUserPrincipal user =
                    (WebSocketAuthenticationInterceptor.WebSocketUserPrincipal) principal;

            log.debug("Received ping from user: {}", user.getEmail());
            // Could send pong back if needed
        }
    }
}

