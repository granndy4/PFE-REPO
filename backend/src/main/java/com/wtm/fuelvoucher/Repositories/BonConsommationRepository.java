package com.wtm.fuelvoucher.Repositories;

import com.wtm.fuelvoucher.Entities.BonConsommation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BonConsommationRepository extends JpaRepository<BonConsommation, Long> {

    boolean existsByReferenceTransaction(String referenceTransaction);
}
