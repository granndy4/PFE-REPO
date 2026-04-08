CREATE TABLE IF NOT EXISTS bons_carburant (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    entreprise_id BIGINT NOT NULL,
    contrat_id BIGINT NULL,
    vehicule_id BIGINT NOT NULL,
    employe_id BIGINT NULL,
    reference_bon VARCHAR(80) NOT NULL,
    qr_nonce VARCHAR(40) NOT NULL,
    qr_code_payload VARCHAR(255) NOT NULL,
    quantite_initiale_litres DECIMAL(10,3) NOT NULL,
    solde_litres DECIMAL(10,3) NOT NULL,
    statut VARCHAR(30) NOT NULL,
    defectueux BIT(1) NOT NULL,
    bon_original_id BIGINT NULL,
    cree_par_utilisateur_id BIGINT NULL,
    cree_le DATETIME(6) NOT NULL,
    modifie_le DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_bons_carburant_reference UNIQUE (reference_bon),
    CONSTRAINT ck_bons_carburant_quantite_positive CHECK (quantite_initiale_litres > 0),
    CONSTRAINT ck_bons_carburant_solde_non_negatif CHECK (solde_litres >= 0)
);

CREATE INDEX idx_bons_carburant_societe ON bons_carburant (societe_id);
CREATE INDEX idx_bons_carburant_entreprise ON bons_carburant (entreprise_id);
CREATE INDEX idx_bons_carburant_vehicule ON bons_carburant (vehicule_id);
CREATE INDEX idx_bons_carburant_statut ON bons_carburant (statut);
CREATE INDEX idx_bons_carburant_bon_original ON bons_carburant (bon_original_id);

CREATE TABLE IF NOT EXISTS bons_consommation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    bon_id BIGINT NOT NULL,
    reference_transaction VARCHAR(120) NOT NULL,
    quantite_litres DECIMAL(10,3) NOT NULL,
    consomme_le DATETIME(6) NOT NULL,
    consomme_par_utilisateur_id BIGINT NULL,
    notes VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_bons_consommation_ref_tx UNIQUE (reference_transaction),
    CONSTRAINT ck_bons_consommation_quantite_positive CHECK (quantite_litres > 0)
);

CREATE INDEX idx_bons_consommation_bon ON bons_consommation (bon_id);
CREATE INDEX idx_bons_consommation_consomme_le ON bons_consommation (consomme_le);
