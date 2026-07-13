package com.youngkke.careon.domain.notification;

import com.youngkke.careon.domain.policy.SavedPolicy;
import com.youngkke.careon.domain.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    void deleteAllBySavedPolicyIn(List<SavedPolicy> savedPolicies);

    List<Notification> findAllBySavedPolicy_UserOrderBySentAtDesc(User user);

    boolean existsBySavedPolicyAndNotificationType(SavedPolicy savedPolicy, NotificationType notificationType);
}
