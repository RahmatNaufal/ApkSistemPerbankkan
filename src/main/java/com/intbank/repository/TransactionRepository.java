package com.intbank.repository;

import com.intbank.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    List<Transaction> findTop5ByAccountIdOrderByCreatedAtDesc(Long accountId);

    List<Transaction> findByAccountIdInOrderByCreatedAtDesc(Collection<Long> accountIds);

    List<Transaction> findByAccountIdAndTypeInAndCreatedAtAfter(
            Long accountId, Collection<com.intbank.domain.TransactionType> types, LocalDateTime from);

    /** Riwayat nasabah: dibatasi hanya rekening milik nasabah yang bersangkutan. */
    @Query("select t from Transaction t " +
            "where t.accountId in :accountIds " +
            "and (:type is null or t.type = :type) " +
            "and (:status is null or t.status = :status) " +
            "and (:keyword is null or lower(t.referenceNumber) like lower(concat('%', :keyword, '%')) " +
            "     or lower(t.description) like lower(concat('%', :keyword, '%'))) " +
            "and (:from is null or t.createdAt >= :from) " +
            "and (:to is null or t.createdAt <= :to)")
    Page<Transaction> searchForCustomer(@Param("accountIds") Collection<Long> accountIds,
                                        @Param("type") com.intbank.domain.TransactionType type,
                                        @Param("status") com.intbank.domain.TransactionStatus status,
                                        @Param("keyword") String keyword,
                                        @Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to,
                                        Pageable pageable);

    @Query("select t from Transaction t " +
            "where (:accountId is null or t.accountId = :accountId) " +
            "and (:type is null or t.type = :type) " +
            "and (:status is null or t.status = :status) " +
            "and (:keyword is null or lower(t.referenceNumber) like lower(concat('%', :keyword, '%')) " +
            "     or lower(t.description) like lower(concat('%', :keyword, '%'))) " +
            "and (:from is null or t.createdAt >= :from) " +
            "and (:to is null or t.createdAt <= :to)")
    Page<Transaction> search(@Param("accountId") Long accountId,
                             @Param("type") com.intbank.domain.TransactionType type,
                             @Param("status") com.intbank.domain.TransactionStatus status,
                             @Param("keyword") String keyword,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to,
                             Pageable pageable);
}
