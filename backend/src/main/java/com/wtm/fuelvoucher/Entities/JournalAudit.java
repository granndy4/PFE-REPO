package com.wtm.fuelvoucher.Entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "journaux_audit")
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "societe_id")
    private Long societeId;

    @Column(name = "utilisateur_id")
    private Long utilisateurId;

    @Column(name = "type_evenement", nullable = false, length = 40)
    private String typeEvenement;

    @Column(name = "nom_entite", nullable = false, length = 80)
    private String nomEntite;

    @Column(name = "id_entite")
    private Long idEntite;

    @Column(name = "action", nullable = false, length = 40)
    private String action;

    @Column(name = "adresse_ip", length = 45)
    private String adresseIp;

    @Column(name = "agent_utilisateur", length = 255)
    private String agentUtilisateur;

    @Lob
    @Column(name = "anciennes_valeurs_json")
    private String anciennesValeursJson;

    @Lob
    @Column(name = "nouvelles_valeurs_json")
    private String nouvellesValeursJson;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @PrePersist
    void prePersist() {
        if (creeLe == null) {
            creeLe = Instant.now();
        }
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

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public String getTypeEvenement() {
        return typeEvenement;
    }

    public void setTypeEvenement(String typeEvenement) {
        this.typeEvenement = typeEvenement;
    }

    public String getNomEntite() {
        return nomEntite;
    }

    public void setNomEntite(String nomEntite) {
        this.nomEntite = nomEntite;
    }

    public Long getIdEntite() {
        return idEntite;
    }

    public void setIdEntite(Long idEntite) {
        this.idEntite = idEntite;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAdresseIp() {
        return adresseIp;
    }

    public void setAdresseIp(String adresseIp) {
        this.adresseIp = adresseIp;
    }

    public String getAgentUtilisateur() {
        return agentUtilisateur;
    }

    public void setAgentUtilisateur(String agentUtilisateur) {
        this.agentUtilisateur = agentUtilisateur;
    }

    public String getAnciennesValeursJson() {
        return anciennesValeursJson;
    }

    public void setAnciennesValeursJson(String anciennesValeursJson) {
        this.anciennesValeursJson = anciennesValeursJson;
    }

    public String getNouvellesValeursJson() {
        return nouvellesValeursJson;
    }

    public void setNouvellesValeursJson(String nouvellesValeursJson) {
        this.nouvellesValeursJson = nouvellesValeursJson;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(Instant creeLe) {
        this.creeLe = creeLe;
    }
}




