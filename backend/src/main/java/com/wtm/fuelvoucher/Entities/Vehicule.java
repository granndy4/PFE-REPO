package com.wtm.fuelvoucher.Entities;

import com.wtm.fuelvoucher.Enums.TypeCarburant;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicules")
public class Vehicule extends BaseEntity {

    @Column(name = "societe_id", nullable = false)
    private Long societeId;

    @Column(name = "entreprise_id", nullable = false)
    private Long entrepriseId;

    @Column(name = "employe_id")
    private Long employeId;

    @Column(name = "immatriculation", nullable = false, length = 40)
    private String immatriculation;

    @Column(name = "code_flotte", length = 50)
    private String codeFlotte;

    @Column(name = "marque", length = 60)
    private String marque;

    @Column(name = "modele", length = 60)
    private String modele;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_carburant", nullable = false, length = 20)
    private TypeCarburant typeCarburant;

    @Column(name = "capacite_reservoir_litres", precision = 10, scale = 3)
    private BigDecimal capaciteReservoirLitres;

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

    public Long getEmployeId() {
        return employeId;
    }

    public void setEmployeId(Long employeId) {
        this.employeId = employeId;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public String getCodeFlotte() {
        return codeFlotte;
    }

    public void setCodeFlotte(String codeFlotte) {
        this.codeFlotte = codeFlotte;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public TypeCarburant getTypeCarburant() {
        return typeCarburant;
    }

    public void setTypeCarburant(TypeCarburant typeCarburant) {
        this.typeCarburant = typeCarburant;
    }

    public BigDecimal getCapaciteReservoirLitres() {
        return capaciteReservoirLitres;
    }

    public void setCapaciteReservoirLitres(BigDecimal capaciteReservoirLitres) {
        this.capaciteReservoirLitres = capaciteReservoirLitres;
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




