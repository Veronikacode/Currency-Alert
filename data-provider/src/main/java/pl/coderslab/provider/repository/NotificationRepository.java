package pl.coderslab.provider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.coderslab.provider.entity.Notification;
import pl.coderslab.provider.enums.NotificationStatus;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStatus(NotificationStatus status);
}
