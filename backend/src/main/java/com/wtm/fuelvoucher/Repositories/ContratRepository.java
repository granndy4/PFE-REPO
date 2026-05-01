package com.wtm.fuelvoucher.Repositories;

import com.wtm.fuelvoucher.Entities.Contrat;
import com.wtm.fuelvoucher.Enums.ContratStatut;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContratRepository extends JpaRepository<Contrat, Long> {

    boolean existsBySocieteIdAndNumeroContrat(Long societeId, String numeroContrat);

    boolean existsBySocieteIdAndNumeroContratAndIdNot(Long societeId, String numeroContrat, Long id);

  long countByStatut(ContratStatut statut);

    @Query("""
            SELECT c
            FROM Contrat c
            WHERE (:societeId IS NULL OR c.societeId = :societeId)
              AND (:entrepriseId IS NULL OR c.entrepriseId = :entrepriseId)
              AND (:statut IS NULL OR c.statut = :statut)
              AND (
                  :search IS NULL
                  OR LOWER(c.numeroContrat) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(c.notes, '')) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<Contrat> search(
            @Param("societeId") Long societeId,
            @Param("entrepriseId") Long entrepriseId,
            @Param("statut") ContratStatut statut,
            @Param("search") String search,
            Pageable pageable);
}



