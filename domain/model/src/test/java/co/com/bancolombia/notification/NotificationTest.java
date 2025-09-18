package co.com.bancolombia.notification;

import co.com.bancolombia.model.notification.Notification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void shouldCreateNotification() {
        Notification notification = new Notification(
                "user@test.com",
                "Subject Test",
                "Message body"
        );

        assertNotNull(notification);
        assertEquals("user@test.com", notification.email());
        assertEquals("Subject Test", notification.subject());
        assertEquals("Message body", notification.message());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Notification n1 = new Notification("user@test.com", "Hello", "Message");
        Notification n2 = new Notification("user@test.com", "Hello", "Message");

        assertEquals(n1, n2);
        assertEquals(n1.hashCode(), n2.hashCode());
    }

    @Test
    void shouldDifferentiateDifferentObjects() {
        Notification n1 = new Notification("user@test.com", "Hello", "Message");
        Notification n2 = new Notification("other@test.com", "Hello", "Message");

        assertNotEquals(n1, n2);
    }

    @Test
    void shouldGenerateToString() {
        Notification notification = new Notification("user@test.com", "Hello", "Message");

        String toString = notification.toString();

        assertTrue(toString.contains("user@test.com"));
        assertTrue(toString.contains("Hello"));
        assertTrue(toString.contains("Message"));
    }
}
