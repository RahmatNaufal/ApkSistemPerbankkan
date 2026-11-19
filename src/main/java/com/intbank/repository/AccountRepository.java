package com.intbank.repository;

import com.intbank.domain.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);

    List<Account> findAllByOwnerCustomerId(Long ownerCustomerId);

    /** Kunci baris saat mengubah saldo (SELECT ... FOR UPDATE). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

    /** Transfer mengunci dua rekening berurutan menurut id menaik (anti-deadlock). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id in :ids order by a.id asc")
    List<Account> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);

    @Query(value = "select coalesce(sum(balance), 0) from accounts", nativeQuery = true)
    BigDecimal sumAllBalances();
}
