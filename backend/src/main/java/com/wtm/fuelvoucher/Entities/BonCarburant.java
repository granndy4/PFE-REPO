package com.wtm.fuelvoucher.Entities;

import com.wtm.fuelvoucher.Enums.BonStatut;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "bons_carburant")
public class BonCarburant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "societe_id", nullable = false)
    private Long societeId;

    @Column(name = "entreprise_id", nullable = false)
    private Long entrepriseId;

    @Column(name = "contrat_id")
    private Long contratId;

    @Column(name = "vehicule_id", nullable = false)
    private Long vehiculeId;

    @Column(name = "employe_id")
    private Long employeId;

    @Column(name = "reference_bon", nullable = false, unique = true, length = 80)
    private String referenceBon;

    @Column(name = "qr_nonce", nullable = false, length = 40)
    private String qrNonce;

    @Column(name = "qr_code_payload", nullable = false, length = 255)
    private String qrCodePayload;

    @Column(name = "quantite_initiale_litres", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantiteInitialeLitres;

    @Column(name = "solde_litres", nullable = false, precision = 10, scale = 3)
    private BigDecimal soldeLitres;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    private BonStatut statut = BonStatut.ISSUED;

    @Column(name = "defectueux", nullable = false)
    private boolean defectueux = false;

    @Column(name = "bon_original_id")
    private Long bonOriginalId;

    @Column(name = "cree_par_utilisateur_id")
    private Long creeParUtilisateurId;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (creeLe == null) {
            creeLe = now;
        }
        if (modifieLe == null) {
            modifieLe = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        modifieLe = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getContratId() {
        return contratId;
    }

    public void setContratId(Long contratId) {
        this.contratId = contratId;
    }

    public Long getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(Long vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public Long getEmployeId() {
        return employeId;
    }

    public void setEmployeId(Long employeId) {
        this.employeId = employeId;
    }

    public String getReferenceBon() {
        return referenceBon;
    }

    public void setReferenceBon(String referenceBon) {
        this.referenceBon = referenceBon;
    }

    public String getQrNonce() {
        return qrNonce;
    }

    public void setQrNonce(String qrNonce) {
        this.qrNonce = qrNonce;
    }

    public String getQrCodePayload() {
        return qrCodePayload;
    }

    public void setQrCodePayload(String qrCodePayload) {
        this.qrCodePayload = qrCodePayload;
    }

    public BigDecimal getQuantiteInitialeLitres() {
        return quantiteInitialeLitres;
    }

    public void setQuantiteInitialeLitres(BigDecimal quantiteInitialeLitres) {
        this.quantiteInitialeLitres = quantiteInitialeLitres;
    }

    public BigDecimal getSoldeLitres() {
        return soldeLitres;
    }

    public void setSoldeLitres(BigDecimal soldeLitres) {
        this.soldeLitres = soldeLitres;
    }

    public BonStatut getStatut() {
        return statut;
    }

    public void setStatut(BonStatut statut) {
        this.statut = statut;
    }

    public boolean isDefectueux() {
        return defectueux;
    }

    public void setDefectueux(boolean defectueux) {
        this.defectueux = defectueux;
    }

    public Long getBonOriginalId() {
        return bonOriginalId;
    }

    public void setBonOriginalId(Long bonOriginalId) {
        this.bonOriginalId = bonOriginalId;
    }

    public Long getCreeParUtilisateurId() {
        return creeParUtilisateurId;
    }

    public void setCreeParUtilisateurId(Long creeParUtilisateurId) {
        this.creeParUtilisateurId = creeParUtilisateurId;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(Instant creeLe) {
        this.creeLe = creeLe;
    }

    public Instant getModifieLe() {
        return modifieLe;
    }

    public void setModifieLe(Instant modifieLe) {
        this.modifieLe = modifieLe;
    }
}
