package com.drew.accountservice.repository;

import com.drew.accountservice.entity.Account;
import com.drew.accountservice.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BalanceRepository extends JpaRepository<Balance, Long>, JpaSpecificationExecutor<Balance> {

    List<Balance> findAllByAccount(Account account);
    Optional<Balance> findTopByAccountOrderByDateUpdatedDesc(Account account);
    Optional<Balance> findByBalanceIdAndAccount(Long balanceId, Account account);

    @Query("SELECT b FROM Balance b WHERE b.account.accountId = :accountId ORDER BY b.dateUpdated DESC")
    List<Balance> findLatestBalanceByAccountId(@Param("accountId") Long accountId);
}
