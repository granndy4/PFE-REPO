package com.wtm.fuelvoucher.Entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "employes_entreprise")
public class EmployeEntreprise extends BaseEntity {

    @Column(name = "societe_id", nullable = false)
    private Long societeId;

    @Column(name = "entreprise_id", nullable = false)
    private Long entrepriseId;

    @Column(name = "code_employe", nullable = false, length = 50)
    private String codeEmploye;

    @Column(name = "nom_complet", nullable = false, length = 140)
    private String nomComplet;

    @Column(name = "cin", length = 40)
    private String cin;

    @Column(name = "telephone", length = 40)
    private String telephone;

    @Column(name = "email", length = 160)
    private String email;

    @Column(name = "poste", length = 100)
    private String poste;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

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

    public String getCodeEmploye() {
        return codeEmploye;
    }

    public void setCodeEmploye(String codeEmploye) {
        this.codeEmploye = codeEmploye;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
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




