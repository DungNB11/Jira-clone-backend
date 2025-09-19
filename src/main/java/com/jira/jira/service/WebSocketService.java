package com.jira.jira.service;

import com.jira.jira.dto.websocket.TaskUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send task update to all users in a workspace
     */
    public void sendTaskUpdateToWorkspace(String workspaceId, TaskUpdateEvent event) {
        String destination = "/topic/workspace/" + workspaceId + "/tasks";

        log.info("Sending task update to workspace {}: {}", workspaceId, event.getEventType());
        messagingTemplate.convertAndSend(destination, event);
    }

    /**
     * Send task update to all users in a project
     */
    public void sendTaskUpdateToProject(String projectId, TaskUpdateEvent event) {
        String destination = "/topic/project/" + projectId + "/tasks";

        log.info("Sending task update to project {}: {}", projectId, event.getEventType());
        messagingTemplate.convertAndSend(destination, event);
    }

    /**
     * Send private notification to a specific user
     */
    public void sendPrivateNotification(String userId, TaskUpdateEvent event) {
        String destination = "/queue/notifications";

        log.info("Sending private notification to user {}: {}", userId, event.getEventType());
        messagingTemplate.convertAndSendToUser(userId, destination, event);
    }

    /**
     * Send task assignment notification to assignee
     */
    public void sendTaskAssignmentNotification(String assigneeId, TaskUpdateEvent event) {
        String destination = "/queue/assignments";

        log.info("Sending assignment notification to user {}: {}", assigneeId, event.getTaskName());
        messagingTemplate.convertAndSendToUser(assigneeId, destination, event);
    }

    /**
     * Broadcast system-wide notification (for important updates)
     */
    public void broadcastSystemNotification(TaskUpdateEvent event) {
        String destination = "/topic/system/notifications";

        log.info("Broadcasting system notification: {}", event.getEventType());
        messagingTemplate.convertAndSend(destination, event);
    }

    /**
     * Send real-time activity feed update
     */
    public void sendActivityUpdate(String workspaceId, TaskUpdateEvent event) {
        String destination = "/topic/workspace/" + workspaceId + "/activity";

        log.info("Sending activity update to workspace {}: {}", workspaceId, event.getMessage());
        messagingTemplate.convertAndSend(destination, event);
    }

    /**
     * Send kanban board update (for drag & drop)
     */
    public void sendKanbanUpdate(String workspaceId, TaskUpdateEvent event) {
        String destination = "/topic/workspace/" + workspaceId + "/kanban";

        log.info("Sending kanban update to workspace {}: task {} moved", workspaceId, event.getTaskName());
        messagingTemplate.convertAndSend(destination, event);
    }

    /**
     * Send user presence update
     */
    public void sendUserPresenceUpdate(String workspaceId, String userId, boolean isOnline) {
        String destination = "/topic/workspace/" + workspaceId + "/presence";

        PresenceEvent presenceEvent = new PresenceEvent(userId, isOnline, System.currentTimeMillis());

        log.info("Sending presence update to workspace {}: user {} is {}",
                workspaceId, userId, isOnline ? "online" : "offline");
        messagingTemplate.convertAndSend(destination, presenceEvent);
    }

    /**
     * Simple presence event for user online/offline status
     */
    public static class PresenceEvent {
        private String userId;
        private boolean isOnline;
        private long timestamp;

        public PresenceEvent(String userId, boolean isOnline, long timestamp) {
            this.userId = userId;
            this.isOnline = isOnline;
            this.timestamp = timestamp;
        }

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public boolean isOnline() { return isOnline; }
        public void setOnline(boolean online) { isOnline = online; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}

