package com.ipintelligence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ipintelligence.model.IpAsset;
import com.ipintelligence.model.Subscription;
import com.ipintelligence.model.SubscriptionStatus;
import com.ipintelligence.model.User;
import java.util.List;
import java.util.Optional;


@Repository
public interface subscriptionRepository extends JpaRepository<Subscription, Integer> {

    boolean existsByUserAndIpAsset(User user, IpAsset ipAsset);

    List<Subscription> findByUser(User user);

    // ✅ THIS IS REQUIRED
    List<Subscription> findByUserAndStatus(User user, SubscriptionStatus status);

    @Query("""
           SELECT s FROM Subscription s
           WHERE s.user = :user
           AND s.ipAsset = :ipAsset
           """)
    Optional<Subscription> findByUserAndIpAsset(
            @Param("user") User user,
            @Param("ipAsset") IpAsset ipAsset
    );
}

