package com.bankflow.repository;

import com.bankflow.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {


    List<TransactionRecord> findByFromAccountIdOrToAccountIdOrderByTimestampDesc(Long fromAccountId, Long toAccountId);

    List<TransactionRecord> findByFromAccountIdOrderByTimestampDesc(Long fromAccountId);
}

