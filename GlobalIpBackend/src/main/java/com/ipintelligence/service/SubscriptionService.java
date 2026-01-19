package com.ipintelligence.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipintelligence.model.IpAsset;
import com.ipintelligence.model.Subscription;
import com.ipintelligence.model.User;
import com.ipintelligence.repo.subscriptionRepository;

import jakarta.transaction.Transactional;



@Service
public class SubscriptionService {

	@Autowired
    subscriptionRepository subscriptionRepository;

    

    @Transactional
    public void unsubscribe(User user, IpAsset ipAsset) {

        Subscription subscription = subscriptionRepository
                .findByUserAndIpAsset(user, ipAsset)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscriptionRepository.delete(subscription);
    }
}


