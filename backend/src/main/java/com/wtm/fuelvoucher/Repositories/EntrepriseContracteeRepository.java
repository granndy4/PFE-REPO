package com.wtm.fuelvoucher.Repositories;

import com.wtm.fuelvoucher.Entities.EntrepriseContractee;
import com.wtm.fuelvoucher.Enums.EntrepriseStatut;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EntrepriseContracteeRepository extends JpaRepository<EntrepriseContractee, Long> {

    boolean existsBySocieteIdAndCodeEntreprise(Long societeId, String codeEntreprise);

    boolean existsBySocieteIdAndCodeEntrepriseAndIdNot(Long societeId, String codeEntreprise, Long id);

    @Query("""
            SELECT e
            FROM EntrepriseContractee e
            WHERE (:societeId IS NULL OR e.societeId = :societeId)
              AND (:statut IS NULL OR e.statut = :statut)
              AND (
                  :search IS NULL
                  OR LOWER(e.codeEntreprise) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(e.raisonSociale) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<EntrepriseContractee> search(
            @Param("societeId") Long societeId,
            @Param("statut") EntrepriseStatut statut,
            @Param("search") String search,
            Pageable pageable);
}




