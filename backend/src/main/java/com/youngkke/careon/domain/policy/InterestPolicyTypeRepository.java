package com.youngkke.careon.domain.policy;

import com.youngkke.careon.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestPolicyTypeRepository extends JpaRepository<InterestPolicyType, Integer> {

    void deleteAllByUser(User user);
}
