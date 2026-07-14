package com.youngkke.careon.domain.policy;

import com.youngkke.careon.domain.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedPolicyRepository extends JpaRepository<SavedPolicy, Integer> {

    List<SavedPolicy> findAllByUser(User user);

    void deleteAllByUser(User user);

    boolean existsByUserAndPolicy(User user, Policy policy);

    Optional<SavedPolicy> findBySavedPolicyIdAndUser(Integer savedPolicyId, User user);
}
