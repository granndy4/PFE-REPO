package com.wtm.fuelvoucher.Repositories;

import com.wtm.fuelvoucher.Dtos.JournalAuditReportResponse;
import com.wtm.fuelvoucher.Entities.JournalAudit;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long> {

    @Query(
	    value = """
		    SELECT new com.wtm.fuelvoucher.Dtos.JournalAuditReportResponse(
			j.id,
			j.societeId,
			j.utilisateurId,
			j.typeEvenement,
			j.nomEntite,
			j.idEntite,
			j.action,
			j.adresseIp,
			j.agentUtilisateur,
			j.anciennesValeursJson,
			j.nouvellesValeursJson,
			j.creeLe
		    )
		    FROM JournalAudit j
		    WHERE (:societeId IS NULL OR j.societeId = :societeId)
		      AND (:nomEntite IS NULL OR LOWER(j.nomEntite) = LOWER(:nomEntite))
		      AND (:idEntite IS NULL OR j.idEntite = :idEntite)
		      AND (:fromInclusive IS NULL OR j.creeLe >= :fromInclusive)
		      AND (:toExclusive IS NULL OR j.creeLe < :toExclusive)
		    ORDER BY j.creeLe DESC
		    """,
	    countQuery = """
		    SELECT COUNT(j)
		    FROM JournalAudit j
		    WHERE (:societeId IS NULL OR j.societeId = :societeId)
		      AND (:nomEntite IS NULL OR LOWER(j.nomEntite) = LOWER(:nomEntite))
		      AND (:idEntite IS NULL OR j.idEntite = :idEntite)
		      AND (:fromInclusive IS NULL OR j.creeLe >= :fromInclusive)
		      AND (:toExclusive IS NULL OR j.creeLe < :toExclusive)
		    """
    )
    Page<JournalAuditReportResponse> searchAuditHistory(
	    @Param("societeId") Long societeId,
	    @Param("nomEntite") String nomEntite,
	    @Param("idEntite") Long idEntite,
	    @Param("fromInclusive") Instant fromInclusive,
	    @Param("toExclusive") Instant toExclusive,
	    Pageable pageable);

    @Query(
	    """
		    SELECT new com.wtm.fuelvoucher.Dtos.JournalAuditReportResponse(
			j.id,
			j.societeId,
			j.utilisateurId,
			j.typeEvenement,
			j.nomEntite,
			j.idEntite,
			j.action,
			j.adresseIp,
			j.agentUtilisateur,
			j.anciennesValeursJson,
			j.nouvellesValeursJson,
			j.creeLe
		    )
		    FROM JournalAudit j
		    WHERE (:societeId IS NULL OR j.societeId = :societeId)
		      AND (:nomEntite IS NULL OR LOWER(j.nomEntite) = LOWER(:nomEntite))
		      AND (:idEntite IS NULL OR j.idEntite = :idEntite)
		      AND (:fromInclusive IS NULL OR j.creeLe >= :fromInclusive)
		      AND (:toExclusive IS NULL OR j.creeLe < :toExclusive)
		    ORDER BY j.creeLe DESC
		    """
    )
    List<JournalAuditReportResponse> listAuditHistory(
	    @Param("societeId") Long societeId,
	    @Param("nomEntite") String nomEntite,
	    @Param("idEntite") Long idEntite,
	    @Param("fromInclusive") Instant fromInclusive,
	    @Param("toExclusive") Instant toExclusive);

    @Query(
	    """
		    SELECT j
		    FROM JournalAudit j
		    WHERE (:societeId IS NULL OR j.societeId = :societeId)
		      AND j.creeLe >= :fromInclusive
		      AND j.creeLe < :toExclusive
		    """
    )
    List<JournalAudit> listForDashboardTrends(
	    @Param("societeId") Long societeId,
	    @Param("fromInclusive") Instant fromInclusive,
	    @Param("toExclusive") Instant toExclusive);
}




