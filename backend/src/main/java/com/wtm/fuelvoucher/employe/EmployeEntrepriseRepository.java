package com.wtm.fuelvoucher.employe;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeEntrepriseRepository extends JpaRepository<EmployeEntreprise, Long> {

    boolean existsByEntrepriseIdAndCodeEmploye(Long entrepriseId, String codeEmploye);

    boolean existsByEntrepriseIdAndCodeEmployeAndIdNot(Long entrepriseId, String codeEmploye, Long id);

    @Query("""
            SELECT e
            FROM EmployeEntreprise e
            WHERE (:societeId IS NULL OR e.societeId = :societeId)
              AND (:entrepriseId IS NULL OR e.entrepriseId = :entrepriseId)
              AND (:actif IS NULL OR e.actif = :actif)
              AND (
                  :search IS NULL
                  OR LOWER(e.codeEmploye) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(e.nomComplet) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(e.cin, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(e.email, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(e.telephone, '')) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<EmployeEntreprise> search(
            @Param("societeId") Long societeId,
            @Param("entrepriseId") Long entrepriseId,
            @Param("actif") Boolean actif,
            @Param("search") String search,
            Pageable pageable);
}
