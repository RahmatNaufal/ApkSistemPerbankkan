package com.intbank.repository;

import com.intbank.domain.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByTransferReference(String transferReference);

    Optional<Transfer> findByTransactionOutId(Long transactionOutId);

    Optional<Transfer> findByTransactionInId(Long transactionInId);
}
