package com.wtm.fuelvoucher.Repositories;

import com.wtm.fuelvoucher.Dtos.BonConsommationReportResponse;
import com.wtm.fuelvoucher.Entities.BonCarburant;
import com.wtm.fuelvoucher.Entities.BonConsommation;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BonConsommationRepository extends JpaRepository<BonConsommation, Long> {

    boolean existsByReferenceTransaction(String referenceTransaction);

        @Query(
                        value = """
                                        SELECT new com.wtm.fuelvoucher.Dtos.BonConsommationReportResponse(
                                                c.id,
                                                c.bonId,
                                                b.referenceBon,
                                                b.societeId,
                                                b.entrepriseId,
                                                b.vehiculeId,
                                                c.referenceTransaction,
                                                c.quantiteLitres,
                                                c.consommeLe,
                                                c.consommeParUtilisateurId,
                                                c.notes
                                        )
                                        FROM BonConsommation c
                                        JOIN BonCarburant b ON b.id = c.bonId
                                        WHERE (:societeId IS NULL OR b.societeId = :societeId)
                                            AND (:entrepriseId IS NULL OR b.entrepriseId = :entrepriseId)
                                            AND (:fromInclusive IS NULL OR c.consommeLe >= :fromInclusive)
                                            AND (:toExclusive IS NULL OR c.consommeLe < :toExclusive)
                                        ORDER BY c.consommeLe DESC
                                        """,
                        countQuery = """
                                        SELECT COUNT(c)
                                        FROM BonConsommation c
                                        JOIN BonCarburant b ON b.id = c.bonId
                                        WHERE (:societeId IS NULL OR b.societeId = :societeId)
                                            AND (:entrepriseId IS NULL OR b.entrepriseId = :entrepriseId)
                                            AND (:fromInclusive IS NULL OR c.consommeLe >= :fromInclusive)
                                            AND (:toExclusive IS NULL OR c.consommeLe < :toExclusive)
                                        """
        )
        Page<BonConsommationReportResponse> searchConsumptionReport(
                        @Param("societeId") Long societeId,
                        @Param("entrepriseId") Long entrepriseId,
                        @Param("fromInclusive") Instant fromInclusive,
                        @Param("toExclusive") Instant toExclusive,
                        Pageable pageable);

        @Query(
                """
                        SELECT new com.wtm.fuelvoucher.Dtos.BonConsommationReportResponse(
                            c.id,
                            c.bonId,
                            b.referenceBon,
                            b.societeId,
                            b.entrepriseId,
                            b.vehiculeId,
                            c.referenceTransaction,
                            c.quantiteLitres,
                            c.consommeLe,
                            c.consommeParUtilisateurId,
                            c.notes
                        )
                        FROM BonConsommation c
                        JOIN BonCarburant b ON b.id = c.bonId
                        WHERE (:societeId IS NULL OR b.societeId = :societeId)
                            AND (:entrepriseId IS NULL OR b.entrepriseId = :entrepriseId)
                            AND (:fromInclusive IS NULL OR c.consommeLe >= :fromInclusive)
                            AND (:toExclusive IS NULL OR c.consommeLe < :toExclusive)
                        ORDER BY c.consommeLe DESC
                        """
        )
        List<BonConsommationReportResponse> listConsumptionReport(
                @Param("societeId") Long societeId,
                @Param("entrepriseId") Long entrepriseId,
                @Param("fromInclusive") Instant fromInclusive,
                @Param("toExclusive") Instant toExclusive);

        @Query(
            """
                SELECT c
                FROM BonConsommation c
                JOIN BonCarburant b ON b.id = c.bonId
                WHERE (:societeId IS NULL OR b.societeId = :societeId)
                    AND c.consommeLe >= :fromInclusive
                    AND c.consommeLe < :toExclusive
                """
        )
        List<BonConsommation> listForDashboardTrends(
            @Param("societeId") Long societeId,
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive);
}
