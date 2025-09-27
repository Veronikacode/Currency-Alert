package pl.coderslab.provider.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "baseCurrency", "targetCurrency", "active"})
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "base_currency", length = 3, nullable = false)
    private String baseCurrency;

    @Column(name = "target_currency", length = 3, nullable = false)
    private String targetCurrency;

    @Column(name = "threshold_percent", precision = 6, scale = 3, nullable = false)
    private BigDecimal thresholdPercent;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Notification> notifications = new HashSet<>();

    public void addNotification(Notification notification) {
        notifications.add(notification);
        notification.setSubscription(this);
    }

    public void removeNotification(Notification notification) {
        notifications.remove(notification);
        notification.setSubscription(null);
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}