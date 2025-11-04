package com.vieiradev.desafiopicpay.repositories;

import com.vieiradev.desafiopicpay.domain.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransationRepository extends JpaRepository<Transaction, Long> {
}
