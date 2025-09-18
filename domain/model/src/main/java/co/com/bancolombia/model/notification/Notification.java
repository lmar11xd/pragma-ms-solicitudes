package co.com.bancolombia.model.notification;

public record Notification(
        String email,
        String subject,
        String message
) {
}
