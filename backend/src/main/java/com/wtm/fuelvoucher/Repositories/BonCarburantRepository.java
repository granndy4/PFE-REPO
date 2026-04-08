package com.wtm.fuelvoucher.Repositories;

import com.wtm.fuelvoucher.Entities.BonCarburant;
import com.wtm.fuelvoucher.Enums.BonStatut;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BonCarburantRepository extends JpaRepository<BonCarburant, Long> {

    boolean existsByReferenceBon(String referenceBon);

    Optional<BonCarburant> findByReferenceBon(String referenceBon);

    @Query("""
            SELECT b
            FROM BonCarburant b
            WHERE (:societeId IS NULL OR b.societeId = :societeId)
              AND (:entrepriseId IS NULL OR b.entrepriseId = :entrepriseId)
              AND (:vehiculeId IS NULL OR b.vehiculeId = :vehiculeId)
              AND (:statut IS NULL OR b.statut = :statut)
              AND (
                  :search IS NULL
                  OR LOWER(b.referenceBon) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(b.qrCodePayload) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<BonCarburant> search(
            @Param("societeId") Long societeId,
            @Param("entrepriseId") Long entrepriseId,
            @Param("vehiculeId") Long vehiculeId,
            @Param("statut") BonStatut statut,
            @Param("search") String search,
            Pageable pageable);
}
