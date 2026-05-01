package com.wtm.fuelvoucher.Entities;

import com.wtm.fuelvoucher.Enums.ContratStatut;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "contrats")
public class Contrat extends BaseEntity {

    @Column(name = "societe_id", nullable = false)
    private Long societeId;

    @Column(name = "entreprise_id", nullable = false)
    private Long entrepriseId;

    @Column(name = "numero_contrat", nullable = false, length = 60)
    private String numeroContrat;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "code_devise", nullable = false, length = 3)
    private String codeDevise = "TND";

    @Column(name = "delai_paiement_jours", nullable = false)
    private Integer delaiPaiementJours = 30;

    @Column(name = "montant_max_mensuel", precision = 14, scale = 3)
    private BigDecimal montantMaxMensuel;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private ContratStatut statut = ContratStatut.ACTIVE;

    @Column(name = "signe_le")
    private LocalDate signeLe;

    @Column(name = "notes")
    private String notes;

    @Column(name = "cree_par_utilisateur_id")
    private Long creeParUtilisateurId;

    public Long getId() {
        return super.getId();
    }

    public void setId(Long id) {
        super.setId(id);
    }

    public Long getSocieteId() {
        return societeId;
    }

    public void setSocieteId(Long societeId) {
        this.societeId = societeId;
    }

    public Long getEntrepriseId() {
        return entrepriseId;
    }

    public void setEntrepriseId(Long entrepriseId) {
        this.entrepriseId = entrepriseId;
    }

    public String getNumeroContrat() {
        return numeroContrat;
    }

    public void setNumeroContrat(String numeroContrat) {
        this.numeroContrat = numeroContrat;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getCodeDevise() {
        return codeDevise;
    }

    public void setCodeDevise(String codeDevise) {
        this.codeDevise = codeDevise;
    }

    public Integer getDelaiPaiementJours() {
        return delaiPaiementJours;
    }

    public void setDelaiPaiementJours(Integer delaiPaiementJours) {
        this.delaiPaiementJours = delaiPaiementJours;
    }

    public BigDecimal getMontantMaxMensuel() {
        return montantMaxMensuel;
    }

    public void setMontantMaxMensuel(BigDecimal montantMaxMensuel) {
        this.montantMaxMensuel = montantMaxMensuel;
    }

    public ContratStatut getStatut() {
        return statut;
    }

    public void setStatut(ContratStatut statut) {
        this.statut = statut;
    }

    public LocalDate getSigneLe() {
        return signeLe;
    }

    public void setSigneLe(LocalDate signeLe) {
        this.signeLe = signeLe;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getCreeParUtilisateurId() {
        return creeParUtilisateurId;
    }

    public void setCreeParUtilisateurId(Long creeParUtilisateurId) {
        this.creeParUtilisateurId = creeParUtilisateurId;
    }

    public Instant getCreeLe() {
        return super.getCreeLe();
    }

    public void setCreeLe(Instant creeLe) {
        super.setCreeLe(creeLe);
    }

    public Instant getModifieLe() {
        return super.getModifieLe();
    }

    public void setModifieLe(Instant modifieLe) {
        super.setModifieLe(modifieLe);
    }
}



