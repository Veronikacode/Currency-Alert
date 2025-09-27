package pl.coderslab.provider.notification;


/**
 * Simple value object encapsulating rendered notification data that can be sent via e-mail.
 */
public record NotificationContent(String subject, String body) {
}
