package com.wtm.fuelvoucher.Entities;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "bons_consommation")
public class BonConsommation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bon_id", nullable = false)
    private Long bonId;

    @Column(name = "reference_transaction", nullable = false, unique = true, length = 120)
    private String referenceTransaction;

    @Column(name = "quantite_litres", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantiteLitres;

    @Column(name = "consomme_le", nullable = false)
    private Instant consommeLe;

    @Column(name = "consomme_par_utilisateur_id")
    private Long consommeParUtilisateurId;

    @Column(name = "notes", length = 255)
    private String notes;

    @PrePersist
    void prePersist() {
        if (consommeLe == null) {
            consommeLe = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBonId() {
        return bonId;
    }

    public void setBonId(Long bonId) {
        this.bonId = bonId;
    }

    public String getReferenceTransaction() {
        return referenceTransaction;
    }

    public void setReferenceTransaction(String referenceTransaction) {
        this.referenceTransaction = referenceTransaction;
    }

    public BigDecimal getQuantiteLitres() {
        return quantiteLitres;
    }

    public void setQuantiteLitres(BigDecimal quantiteLitres) {
        this.quantiteLitres = quantiteLitres;
    }

    public Instant getConsommeLe() {
        return consommeLe;
    }

    public void setConsommeLe(Instant consommeLe) {
        this.consommeLe = consommeLe;
    }

    public Long getConsommeParUtilisateurId() {
        return consommeParUtilisateurId;
    }

    public void setConsommeParUtilisateurId(Long consommeParUtilisateurId) {
        this.consommeParUtilisateurId = consommeParUtilisateurId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
