package com.drew.accountservice.repository;

import com.drew.accountservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByKeycloakUserId(String keycloakId);
    Optional<Account> findByAccountIdAndKeycloakUserId(Long accountId, String keycloakUserId);
    Optional<Account> findByTruelayerAccountIdAndKeycloakUserId(String truelayerAccountId, String keycloakUserId);
}
