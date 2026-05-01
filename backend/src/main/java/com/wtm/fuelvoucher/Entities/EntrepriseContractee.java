package com.wtm.fuelvoucher.Entities;

import com.wtm.fuelvoucher.Enums.EntrepriseStatut;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "entreprises_contractees")
public class EntrepriseContractee extends BaseEntity {

    @Column(name = "societe_id", nullable = false)
    private Long societeId;

    @Column(name = "code_entreprise", nullable = false, length = 50)
    private String codeEntreprise;

    @Column(name = "raison_sociale", nullable = false, length = 200)
    private String raisonSociale;

    @Column(name = "nom_court", length = 120)
    private String nomCourt;

    @Column(name = "matricule_fiscal", length = 80)
    private String matriculeFiscal;

    @Column(name = "adresse_facturation")
    private String adresseFacturation;

    @Column(name = "nom_contact", length = 120)
    private String nomContact;

    @Column(name = "email_contact", length = 160)
    private String emailContact;

    @Column(name = "telephone_contact", length = 40)
    private String telephoneContact;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private EntrepriseStatut statut = EntrepriseStatut.ACTIVE;

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

    public String getCodeEntreprise() {
        return codeEntreprise;
    }

    public void setCodeEntreprise(String codeEntreprise) {
        this.codeEntreprise = codeEntreprise;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public String getNomCourt() {
        return nomCourt;
    }

    public void setNomCourt(String nomCourt) {
        this.nomCourt = nomCourt;
    }

    public String getMatriculeFiscal() {
        return matriculeFiscal;
    }

    public void setMatriculeFiscal(String matriculeFiscal) {
        this.matriculeFiscal = matriculeFiscal;
    }

    public String getAdresseFacturation() {
        return adresseFacturation;
    }

    public void setAdresseFacturation(String adresseFacturation) {
        this.adresseFacturation = adresseFacturation;
    }

    public String getNomContact() {
        return nomContact;
    }

    public void setNomContact(String nomContact) {
        this.nomContact = nomContact;
    }

    public String getEmailContact() {
        return emailContact;
    }

    public void setEmailContact(String emailContact) {
        this.emailContact = emailContact;
    }

    public String getTelephoneContact() {
        return telephoneContact;
    }

    public void setTelephoneContact(String telephoneContact) {
        this.telephoneContact = telephoneContact;
    }

    public EntrepriseStatut getStatut() {
        return statut;
    }

    public void setStatut(EntrepriseStatut statut) {
        this.statut = statut;
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




