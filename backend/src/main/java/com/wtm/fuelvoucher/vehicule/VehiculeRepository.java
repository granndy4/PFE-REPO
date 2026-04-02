package com.wtm.fuelvoucher.vehicule;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    boolean existsBySocieteIdAndImmatriculation(Long societeId, String immatriculation);

    boolean existsBySocieteIdAndImmatriculationAndIdNot(Long societeId, String immatriculation, Long id);

    @Query("""
            SELECT v
            FROM Vehicule v
            WHERE (:societeId IS NULL OR v.societeId = :societeId)
              AND (:entrepriseId IS NULL OR v.entrepriseId = :entrepriseId)
              AND (:actif IS NULL OR v.actif = :actif)
              AND (
                  :search IS NULL
                  OR LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(v.codeFlotte, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(v.marque, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(v.modele, '')) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<Vehicule> search(
            @Param("societeId") Long societeId,
            @Param("entrepriseId") Long entrepriseId,
            @Param("actif") Boolean actif,
            @Param("search") String search,
            Pageable pageable);
}
