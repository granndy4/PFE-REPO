-- ============================================================================
-- Gestion des Bons de Carburant - Schema SQL complet (v1)
-- Cible: MySQL 8.0+
-- ============================================================================

CREATE DATABASE IF NOT EXISTS fuel_voucher
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE fuel_voucher;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS lignes_lot_migration;
DROP TABLE IF EXISTS lots_migration;
DROP TABLE IF EXISTS metriques_journalieres;
DROP TABLE IF EXISTS journaux_audit;
DROP TABLE IF EXISTS paiements_facture;
DROP TABLE IF EXISTS lignes_facture;
DROP TABLE IF EXISTS factures;
DROP TABLE IF EXISTS lignes_bon_livraison;
DROP TABLE IF EXISTS bons_livraison;
DROP TABLE IF EXISTS evenements_consommation_externe;
DROP TABLE IF EXISTS regenerations_bon;
DROP TABLE IF EXISTS transactions_bon;
DROP TABLE IF EXISTS evenements_scan_bon;
DROP TABLE IF EXISTS bons_carburant;
DROP TABLE IF EXISTS dotations_contrat;
DROP TABLE IF EXISTS contrats_etablissements;
DROP TABLE IF EXISTS contrats;
DROP TABLE IF EXISTS prix_carburant_etablissement;
DROP TABLE IF EXISTS produits_carburant_societe;
DROP TABLE IF EXISTS produits_carburant;
DROP TABLE IF EXISTS vehicules;
DROP TABLE IF EXISTS employes_entreprise;
DROP TABLE IF EXISTS contacts_entreprise;
DROP TABLE IF EXISTS entreprises_contractees;
DROP TABLE IF EXISTS acces_utilisateur_etablissement;
DROP TABLE IF EXISTS utilisateurs_roles;
DROP TABLE IF EXISTS roles_permissions;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS utilisateurs;
DROP TABLE IF EXISTS etablissements;
DROP TABLE IF EXISTS societes;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- Societes / etablissements
-- ============================================================================

CREATE TABLE societes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code_societe VARCHAR(40) NOT NULL,
    raison_sociale VARCHAR(180) NOT NULL,
    nom_commercial VARCHAR(180) NULL,
    matricule_fiscal VARCHAR(60) NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    langue_defaut VARCHAR(5) NOT NULL DEFAULT 'fr',
    fuseau_horaire VARCHAR(60) NOT NULL DEFAULT 'Africa/Tunis',
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    modifie_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_societes_code (code_societe),
    CHECK (statut IN ('ACTIVE', 'INACTIVE')),
    CHECK (langue_defaut IN ('fr', 'ar', 'en'))
);

CREATE TABLE etablissements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    code_etablissement VARCHAR(40) NOT NULL,
    nom VARCHAR(180) NOT NULL,
    type_etablissement VARCHAR(20) NOT NULL DEFAULT 'STATION',
    adresse_ligne_1 VARCHAR(255) NULL,
    adresse_ligne_2 VARCHAR(255) NULL,
    ville VARCHAR(120) NULL,
    gouvernorat VARCHAR(120) NULL,
    code_pays CHAR(2) NOT NULL DEFAULT 'TN',
    telephone VARCHAR(40) NULL,
    email VARCHAR(160) NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    modifie_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_etablissements_societe_code (societe_id, code_etablissement),
    KEY idx_etablissements_societe_actif (societe_id, actif),
    CONSTRAINT fk_etablissements_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CHECK (type_etablissement IN ('HQ', 'STATION', 'BRANCH'))
);

-- ============================================================================
-- Securite / comptes / droits
-- ============================================================================

CREATE TABLE utilisateurs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NULL,
    etablissement_defaut_id BIGINT NULL,
    nom VARCHAR(120) NOT NULL,
    prenom VARCHAR(120) NULL,
    nom_famille VARCHAR(120) NULL,
    email VARCHAR(160) NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role_systeme VARCHAR(20) NOT NULL DEFAULT 'USER',
    langue_preferee VARCHAR(5) NOT NULL DEFAULT 'fr',
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    derniere_connexion_le DATETIME(3) NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    modifie_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_utilisateurs_email (email),
    KEY idx_utilisateurs_societe_actif (societe_id, actif),
    KEY idx_utilisateurs_etablissement_defaut (etablissement_defaut_id),
    CONSTRAINT fk_utilisateurs_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_utilisateurs_etablissement_defaut
        FOREIGN KEY (etablissement_defaut_id) REFERENCES etablissements (id),
    CHECK (role_systeme IN ('ADMIN', 'MANAGER', 'AGENT', 'ACCOUNTANT', 'USER')),
    CHECK (langue_preferee IN ('fr', 'ar', 'en'))
);

CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code_role VARCHAR(40) NOT NULL,
    nom_role VARCHAR(120) NOT NULL,
    est_systeme BOOLEAN NOT NULL DEFAULT TRUE,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_roles_code (code_role)
);

CREATE TABLE permissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code_permission VARCHAR(80) NOT NULL,
    description VARCHAR(255) NOT NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_permissions_code (code_permission)
);

CREATE TABLE roles_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    accorde_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_roles_permissions_role
        FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_roles_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

CREATE TABLE utilisateurs_roles (
    utilisateur_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigne_par_utilisateur_id BIGINT NULL,
    assigne_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (utilisateur_id, role_id),
    KEY idx_utilisateurs_roles_role (role_id),
    KEY idx_utilisateurs_roles_assigne_par (assigne_par_utilisateur_id),
    CONSTRAINT fk_utilisateurs_roles_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id),
    CONSTRAINT fk_utilisateurs_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_utilisateurs_roles_assigne_par
        FOREIGN KEY (assigne_par_utilisateur_id) REFERENCES utilisateurs (id)
);

CREATE TABLE acces_utilisateur_etablissement (
    utilisateur_id BIGINT NOT NULL,
    etablissement_id BIGINT NOT NULL,
    niveau_acces VARCHAR(20) NOT NULL DEFAULT 'READ_WRITE',
    accorde_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (utilisateur_id, etablissement_id),
    KEY idx_acces_utilisateur_etablissement_etablissement (etablissement_id),
    CONSTRAINT fk_acces_utilisateur_etablissement_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id),
    CONSTRAINT fk_acces_utilisateur_etablissement_etablissement
        FOREIGN KEY (etablissement_id) REFERENCES etablissements (id),
    CHECK (niveau_acces IN ('READ_ONLY', 'READ_WRITE', 'ADMIN'))
);

-- ============================================================================
-- Referentiel metier
-- ============================================================================

CREATE TABLE entreprises_contractees (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    code_entreprise VARCHAR(50) NOT NULL,
    raison_sociale VARCHAR(200) NOT NULL,
    nom_court VARCHAR(120) NULL,
    matricule_fiscal VARCHAR(80) NULL,
    adresse_facturation TEXT NULL,
    nom_contact VARCHAR(120) NULL,
    email_contact VARCHAR(160) NULL,
    telephone_contact VARCHAR(40) NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    modifie_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_entreprises_contractees_societe_code (societe_id, code_entreprise),
    KEY idx_entreprises_contractees_societe_statut (societe_id, statut),
    CONSTRAINT fk_entreprises_contractees_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CHECK (statut IN ('ACTIVE', 'SUSPENDED', 'CLOSED'))
);

CREATE TABLE contacts_entreprise (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    entreprise_id BIGINT NOT NULL,
    nom_complet VARCHAR(120) NOT NULL,
    email VARCHAR(160) NULL,
    telephone VARCHAR(40) NULL,
    est_contact_facturation BOOLEAN NOT NULL DEFAULT FALSE,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_contacts_entreprise_entreprise (entreprise_id),
    CONSTRAINT fk_contacts_entreprise_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_contacts_entreprise_entreprise
        FOREIGN KEY (entreprise_id) REFERENCES entreprises_contractees (id)
);

CREATE TABLE employes_entreprise (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    entreprise_id BIGINT NOT NULL,
    code_employe VARCHAR(50) NOT NULL,
    nom_complet VARCHAR(140) NOT NULL,
    cin VARCHAR(40) NULL,
    telephone VARCHAR(40) NULL,
    email VARCHAR(160) NULL,
    poste VARCHAR(100) NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    modifie_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_employes_entreprise_code (entreprise_id, code_employe),
    KEY idx_employes_entreprise_entreprise_actif (entreprise_id, actif),
    CONSTRAINT fk_employes_entreprise_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_employes_entreprise_entreprise
        FOREIGN KEY (entreprise_id) REFERENCES entreprises_contractees (id)
);

CREATE TABLE vehicules (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    entreprise_id BIGINT NOT NULL,
    employe_id BIGINT NULL,
    immatriculation VARCHAR(40) NOT NULL,
    code_flotte VARCHAR(50) NULL,
    marque VARCHAR(60) NULL,
    modele VARCHAR(60) NULL,
    type_carburant VARCHAR(20) NOT NULL,
    capacite_reservoir_litres DECIMAL(10, 3) NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    modifie_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_vehicules_societe_immatriculation (societe_id, immatriculation),
    KEY idx_vehicules_entreprise_actif (entreprise_id, actif),
    KEY idx_vehicules_employe (employe_id),
    CONSTRAINT fk_vehicules_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_vehicules_entreprise
        FOREIGN KEY (entreprise_id) REFERENCES entreprises_contractees (id),
    CONSTRAINT fk_vehicules_employe
        FOREIGN KEY (employe_id) REFERENCES employes_entreprise (id),
    CHECK (type_carburant IN ('GASOLINE', 'DIESEL', 'GPL', 'ELECTRIC', 'OTHER')),
    CHECK (capacite_reservoir_litres IS NULL OR capacite_reservoir_litres > 0)
);

CREATE TABLE produits_carburant (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code_produit VARCHAR(30) NOT NULL,
    libelle VARCHAR(80) NOT NULL,
    unite VARCHAR(20) NOT NULL DEFAULT 'LITER',
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_produits_carburant_code (code_produit),
    CHECK (unite IN ('LITER', 'KWH', 'OTHER'))
);

CREATE TABLE produits_carburant_societe (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    produit_carburant_id BIGINT NOT NULL,
    prix_unitaire_defaut DECIMAL(12, 3) NOT NULL DEFAULT 0,
    code_devise CHAR(3) NOT NULL DEFAULT 'TND',
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    effectif_de DATE NOT NULL,
    effectif_jusqua DATE NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_produits_carburant_societe_periode (societe_id, produit_carburant_id, effectif_de),
    KEY idx_produits_carburant_societe_lookup (societe_id, produit_carburant_id, actif),
    CONSTRAINT fk_produits_carburant_societe_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_produits_carburant_societe_produit
        FOREIGN KEY (produit_carburant_id) REFERENCES produits_carburant (id),
    CHECK (prix_unitaire_defaut >= 0),
    CHECK (effectif_jusqua IS NULL OR effectif_jusqua >= effectif_de)
);

CREATE TABLE prix_carburant_etablissement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    etablissement_id BIGINT NOT NULL,
    produit_carburant_id BIGINT NOT NULL,
    prix_unitaire DECIMAL(12, 3) NOT NULL,
    code_devise CHAR(3) NOT NULL DEFAULT 'TND',
    effectif_de DATETIME(3) NOT NULL,
    effectif_jusqua DATETIME(3) NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_prix_carburant_etablissement_periode (etablissement_id, produit_carburant_id, effectif_de),
    KEY idx_prix_carburant_etablissement_lookup (societe_id, etablissement_id, produit_carburant_id),
    CONSTRAINT fk_prix_carburant_etablissement_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_prix_carburant_etablissement_etablissement
        FOREIGN KEY (etablissement_id) REFERENCES etablissements (id),
    CONSTRAINT fk_prix_carburant_etablissement_produit
        FOREIGN KEY (produit_carburant_id) REFERENCES produits_carburant (id),
    CHECK (prix_unitaire >= 0),
    CHECK (effectif_jusqua IS NULL OR effectif_jusqua >= effectif_de)
);

-- ============================================================================
-- Contrats / dotations
-- ============================================================================

CREATE TABLE contrats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    entreprise_id BIGINT NOT NULL,
    numero_contrat VARCHAR(60) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NULL,
    code_devise CHAR(3) NOT NULL DEFAULT 'TND',
    delai_paiement_jours INT NOT NULL DEFAULT 30,
    montant_max_mensuel DECIMAL(14, 3) NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    signe_le DATE NULL,
    notes TEXT NULL,
    cree_par_utilisateur_id BIGINT NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    modifie_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_contrats_societe_numero (societe_id, numero_contrat),
    KEY idx_contrats_entreprise_statut (entreprise_id, statut),
    KEY idx_contrats_periode (date_debut, date_fin),
    CONSTRAINT fk_contrats_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_contrats_entreprise
        FOREIGN KEY (entreprise_id) REFERENCES entreprises_contractees (id),
    CONSTRAINT fk_contrats_cree_par
        FOREIGN KEY (cree_par_utilisateur_id) REFERENCES utilisateurs (id),
    CHECK (statut IN ('DRAFT', 'ACTIVE', 'SUSPENDED', 'TERMINATED', 'EXPIRED')),
    CHECK (delai_paiement_jours >= 0),
    CHECK (montant_max_mensuel IS NULL OR montant_max_mensuel >= 0),
    CHECK (date_fin IS NULL OR date_fin >= date_debut)
);

CREATE TABLE contrats_etablissements (
    contrat_id BIGINT NOT NULL,
    etablissement_id BIGINT NOT NULL,
    lie_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (contrat_id, etablissement_id),
    KEY idx_contrats_etablissements_etablissement (etablissement_id),
    CONSTRAINT fk_contrats_etablissements_contrat
        FOREIGN KEY (contrat_id) REFERENCES contrats (id),
    CONSTRAINT fk_contrats_etablissements_etablissement
        FOREIGN KEY (etablissement_id) REFERENCES etablissements (id)
);

CREATE TABLE dotations_contrat (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    contrat_id BIGINT NOT NULL,
    entreprise_id BIGINT NOT NULL,
    type_cible VARCHAR(20) NOT NULL,
    employe_id BIGINT NULL,
    vehicule_id BIGINT NULL,
    produit_carburant_id BIGINT NOT NULL,
    periodicite VARCHAR(20) NOT NULL,
    quantite_allouee DECIMAL(12, 3) NOT NULL,
    max_par_transaction DECIMAL(12, 3) NULL,
    report_autorise BOOLEAN NOT NULL DEFAULT FALSE,
    valide_de DATE NOT NULL,
    valide_jusqua DATE NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    modifie_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_dotations_contrat_actif (contrat_id, actif),
    KEY idx_dotations_employe (employe_id),
    KEY idx_dotations_vehicule (vehicule_id),
    KEY idx_dotations_produit (produit_carburant_id),
    CONSTRAINT fk_dotations_contrat_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_dotations_contrat_contrat
        FOREIGN KEY (contrat_id) REFERENCES contrats (id),
    CONSTRAINT fk_dotations_contrat_entreprise
        FOREIGN KEY (entreprise_id) REFERENCES entreprises_contractees (id),
    CONSTRAINT fk_dotations_contrat_employe
        FOREIGN KEY (employe_id) REFERENCES employes_entreprise (id),
    CONSTRAINT fk_dotations_contrat_vehicule
        FOREIGN KEY (vehicule_id) REFERENCES vehicules (id),
    CONSTRAINT fk_dotations_contrat_produit
        FOREIGN KEY (produit_carburant_id) REFERENCES produits_carburant (id),
    CHECK (type_cible IN ('COMPANY', 'EMPLOYEE', 'VEHICLE')),
    CHECK (periodicite IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
    CHECK (
        (type_cible = 'COMPANY' AND employe_id IS NULL AND vehicule_id IS NULL) OR
        (type_cible = 'EMPLOYEE' AND employe_id IS NOT NULL AND vehicule_id IS NULL) OR
        (type_cible = 'VEHICLE' AND employe_id IS NULL AND vehicule_id IS NOT NULL)
    ),
    CHECK (quantite_allouee >= 0),
    CHECK (max_par_transaction IS NULL OR max_par_transaction > 0),
    CHECK (valide_jusqua IS NULL OR valide_jusqua >= valide_de)
);

-- ============================================================================
-- Bons de carburant / scan / transactions
-- ============================================================================

CREATE TABLE bons_carburant (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    contrat_id BIGINT NOT NULL,
    entreprise_id BIGINT NOT NULL,
    etablissement_id BIGINT NOT NULL,
    employe_id BIGINT NULL,
    vehicule_id BIGINT NULL,
    produit_carburant_id BIGINT NOT NULL,
    emis_par_utilisateur_id BIGINT NULL,
    bon_source_id BIGINT NULL,
    reference_bon VARCHAR(64) NOT NULL,
    reference_qr VARCHAR(128) NOT NULL,
    emis_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    valide_de DATETIME(3) NOT NULL,
    valide_jusqua DATETIME(3) NOT NULL,
    quantite_autorisee DECIMAL(12, 3) NOT NULL,
    quantite_consommee DECIMAL(12, 3) NOT NULL DEFAULT 0,
    quantite_restante DECIMAL(12, 3) NOT NULL,
    prix_unitaire_emission DECIMAL(12, 3) NULL,
    code_devise CHAR(3) NOT NULL DEFAULT 'TND',
    statut VARCHAR(30) NOT NULL DEFAULT 'ISSUED',
    nombre_impressions INT NOT NULL DEFAULT 0,
    dernier_scan_le DATETIME(3) NULL,
    statut_sync_externe VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    modifie_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_bons_carburant_societe_reference (societe_id, reference_bon),
    UNIQUE KEY uq_bons_carburant_societe_qr (societe_id, reference_qr),
    KEY idx_bons_carburant_contrat_statut (contrat_id, statut),
    KEY idx_bons_carburant_entreprise_periode (entreprise_id, valide_de, valide_jusqua),
    KEY idx_bons_carburant_employe (employe_id),
    KEY idx_bons_carburant_vehicule (vehicule_id),
    KEY idx_bons_carburant_etablissement_statut (etablissement_id, statut),
    CONSTRAINT fk_bons_carburant_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_bons_carburant_contrat
        FOREIGN KEY (contrat_id) REFERENCES contrats (id),
    CONSTRAINT fk_bons_carburant_entreprise
        FOREIGN KEY (entreprise_id) REFERENCES entreprises_contractees (id),
    CONSTRAINT fk_bons_carburant_etablissement
        FOREIGN KEY (etablissement_id) REFERENCES etablissements (id),
    CONSTRAINT fk_bons_carburant_employe
        FOREIGN KEY (employe_id) REFERENCES employes_entreprise (id),
    CONSTRAINT fk_bons_carburant_vehicule
        FOREIGN KEY (vehicule_id) REFERENCES vehicules (id),
    CONSTRAINT fk_bons_carburant_produit
        FOREIGN KEY (produit_carburant_id) REFERENCES produits_carburant (id),
    CONSTRAINT fk_bons_carburant_emis_par
        FOREIGN KEY (emis_par_utilisateur_id) REFERENCES utilisateurs (id),
    CONSTRAINT fk_bons_carburant_source
        FOREIGN KEY (bon_source_id) REFERENCES bons_carburant (id),
    CHECK (valide_jusqua >= valide_de),
    CHECK (quantite_autorisee >= 0),
    CHECK (quantite_consommee >= 0),
    CHECK (quantite_restante >= 0),
    CHECK (quantite_consommee <= quantite_autorisee),
    CHECK (statut IN (
        'DRAFT', 'ISSUED', 'PARTIALLY_CONSUMED', 'CONSUMED', 'EXPIRED',
        'CANCELLED', 'DEFECTIVE', 'REGENERATED'
    )),
    CHECK (statut_sync_externe IN ('PENDING', 'SYNCED', 'FAILED'))
);

CREATE TABLE evenements_scan_bon (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    reference_qr_scannee VARCHAR(128) NOT NULL,
    bon_carburant_id BIGINT NULL,
    etablissement_id BIGINT NULL,
    scanne_par_utilisateur_id BIGINT NULL,
    scanne_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    statut_scan VARCHAR(25) NOT NULL,
    message_resultat VARCHAR(255) NULL,
    identifiant_appareil VARCHAR(80) NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_evenements_scan_bon_qr_temps (reference_qr_scannee, scanne_le),
    KEY idx_evenements_scan_bon_bon (bon_carburant_id),
    KEY idx_evenements_scan_bon_etablissement (etablissement_id),
    CONSTRAINT fk_evenements_scan_bon_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_evenements_scan_bon_bon
        FOREIGN KEY (bon_carburant_id) REFERENCES bons_carburant (id),
    CONSTRAINT fk_evenements_scan_bon_etablissement
        FOREIGN KEY (etablissement_id) REFERENCES etablissements (id),
    CONSTRAINT fk_evenements_scan_bon_utilisateur
        FOREIGN KEY (scanne_par_utilisateur_id) REFERENCES utilisateurs (id),
    CHECK (statut_scan IN ('VALID', 'INVALID', 'EXPIRED', 'ALREADY_USED', 'CANCELLED', 'ERROR'))
);

CREATE TABLE transactions_bon (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    bon_carburant_id BIGINT NOT NULL,
    type_evenement VARCHAR(30) NOT NULL,
    evenement_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    delta_quantite DECIMAL(12, 3) NOT NULL DEFAULT 0,
    quantite_avant DECIMAL(12, 3) NULL,
    quantite_apres DECIMAL(12, 3) NULL,
    etablissement_id BIGINT NULL,
    effectue_par_utilisateur_id BIGINT NULL,
    id_evenement_externe VARCHAR(100) NULL,
    identifiant_terminal VARCHAR(80) NULL,
    code_resultat VARCHAR(40) NULL,
    message_resultat VARCHAR(255) NULL,
    payload_json LONGTEXT NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_transactions_bon_evenement_externe (societe_id, id_evenement_externe),
    KEY idx_transactions_bon_bon_temps (bon_carburant_id, evenement_le),
    KEY idx_transactions_bon_type_temps (type_evenement, evenement_le),
    KEY idx_transactions_bon_etablissement_temps (etablissement_id, evenement_le),
    CONSTRAINT fk_transactions_bon_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_transactions_bon_bon
        FOREIGN KEY (bon_carburant_id) REFERENCES bons_carburant (id),
    CONSTRAINT fk_transactions_bon_etablissement
        FOREIGN KEY (etablissement_id) REFERENCES etablissements (id),
    CONSTRAINT fk_transactions_bon_utilisateur
        FOREIGN KEY (effectue_par_utilisateur_id) REFERENCES utilisateurs (id),
    CHECK (type_evenement IN (
        'ISSUE', 'SCAN_VALIDATION', 'CONSUMPTION', 'REGENERATION',
        'CANCELLATION', 'REJECTED_SCAN', 'ADJUSTMENT'
    ))
);

CREATE TABLE regenerations_bon (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    bon_original_id BIGINT NOT NULL,
    nouveau_bon_id BIGINT NOT NULL,
    raison VARCHAR(255) NOT NULL,
    quantite_restante_transferee DECIMAL(12, 3) NOT NULL,
    cree_par_utilisateur_id BIGINT NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_regenerations_bon_nouveau (nouveau_bon_id),
    KEY idx_regenerations_bon_original (bon_original_id),
    CONSTRAINT fk_regenerations_bon_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_regenerations_bon_original
        FOREIGN KEY (bon_original_id) REFERENCES bons_carburant (id),
    CONSTRAINT fk_regenerations_bon_nouveau
        FOREIGN KEY (nouveau_bon_id) REFERENCES bons_carburant (id),
    CONSTRAINT fk_regenerations_bon_cree_par
        FOREIGN KEY (cree_par_utilisateur_id) REFERENCES utilisateurs (id),
    CHECK (quantite_restante_transferee >= 0)
);

CREATE TABLE evenements_consommation_externe (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    systeme_externe VARCHAR(80) NOT NULL,
    id_evenement_externe VARCHAR(120) NOT NULL,
    reference_bon VARCHAR(64) NULL,
    code_produit_carburant VARCHAR(30) NULL,
    quantite DECIMAL(12, 3) NULL,
    evenement_le DATETIME(3) NOT NULL,
    statut_traitement VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    traite_le DATETIME(3) NULL,
    message_erreur VARCHAR(255) NULL,
    payload_brut LONGTEXT NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_evenements_consommation_externe_identite (societe_id, systeme_externe, id_evenement_externe),
    KEY idx_evenements_consommation_externe_traitement (statut_traitement, evenement_le),
    CONSTRAINT fk_evenements_consommation_externe_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CHECK (statut_traitement IN ('RECEIVED', 'PROCESSED', 'FAILED', 'DUPLICATE'))
);

-- ============================================================================
-- Bons de livraison / facturation
-- ============================================================================

CREATE TABLE bons_livraison (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    numero_bon_livraison VARCHAR(60) NOT NULL,
    contrat_id BIGINT NOT NULL,
    entreprise_id BIGINT NOT NULL,
    etablissement_id BIGINT NOT NULL,
    date_livraison DATE NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    montant_ht DECIMAL(14, 3) NOT NULL DEFAULT 0,
    montant_taxe DECIMAL(14, 3) NOT NULL DEFAULT 0,
    montant_ttc DECIMAL(14, 3) NOT NULL DEFAULT 0,
    genere_par_utilisateur_id BIGINT NULL,
    notes TEXT NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    modifie_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_bons_livraison_societe_numero (societe_id, numero_bon_livraison),
    KEY idx_bons_livraison_entreprise_date (entreprise_id, date_livraison),
    CONSTRAINT fk_bons_livraison_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_bons_livraison_contrat
        FOREIGN KEY (contrat_id) REFERENCES contrats (id),
    CONSTRAINT fk_bons_livraison_entreprise
        FOREIGN KEY (entreprise_id) REFERENCES entreprises_contractees (id),
    CONSTRAINT fk_bons_livraison_etablissement
        FOREIGN KEY (etablissement_id) REFERENCES etablissements (id),
    CONSTRAINT fk_bons_livraison_genere_par
        FOREIGN KEY (genere_par_utilisateur_id) REFERENCES utilisateurs (id),
    CHECK (statut IN ('DRAFT', 'ISSUED', 'CANCELLED')),
    CHECK (montant_ht >= 0),
    CHECK (montant_taxe >= 0),
    CHECK (montant_ttc >= 0)
);

CREATE TABLE lignes_bon_livraison (
    id BIGINT NOT NULL AUTO_INCREMENT,
    bon_livraison_id BIGINT NOT NULL,
    bon_carburant_id BIGINT NULL,
    produit_carburant_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantite DECIMAL(12, 3) NOT NULL,
    prix_unitaire DECIMAL(12, 3) NOT NULL,
    montant_ligne DECIMAL(14, 3) NOT NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_lignes_bon_livraison_bon (bon_livraison_id),
    KEY idx_lignes_bon_livraison_bon_carburant (bon_carburant_id),
    CONSTRAINT fk_lignes_bon_livraison_bon
        FOREIGN KEY (bon_livraison_id) REFERENCES bons_livraison (id),
    CONSTRAINT fk_lignes_bon_livraison_bon_carburant
        FOREIGN KEY (bon_carburant_id) REFERENCES bons_carburant (id),
    CONSTRAINT fk_lignes_bon_livraison_produit
        FOREIGN KEY (produit_carburant_id) REFERENCES produits_carburant (id),
    CHECK (quantite >= 0),
    CHECK (prix_unitaire >= 0),
    CHECK (montant_ligne >= 0)
);

CREATE TABLE factures (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    numero_facture VARCHAR(60) NOT NULL,
    contrat_id BIGINT NOT NULL,
    entreprise_id BIGINT NOT NULL,
    periode_facturation_debut DATE NOT NULL,
    periode_facturation_fin DATE NOT NULL,
    date_emission DATE NOT NULL,
    date_echeance DATE NULL,
    code_devise CHAR(3) NOT NULL DEFAULT 'TND',
    statut VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    montant_ht DECIMAL(14, 3) NOT NULL DEFAULT 0,
    montant_taxe DECIMAL(14, 3) NOT NULL DEFAULT 0,
    montant_ttc DECIMAL(14, 3) NOT NULL DEFAULT 0,
    montant_paye DECIMAL(14, 3) NOT NULL DEFAULT 0,
    montant_restant DECIMAL(14, 3) NOT NULL DEFAULT 0,
    cree_par_utilisateur_id BIGINT NULL,
    notes TEXT NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    modifie_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_factures_societe_numero (societe_id, numero_facture),
    KEY idx_factures_entreprise_periode (entreprise_id, periode_facturation_debut, periode_facturation_fin),
    KEY idx_factures_statut_echeance (statut, date_echeance),
    CONSTRAINT fk_factures_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_factures_contrat
        FOREIGN KEY (contrat_id) REFERENCES contrats (id),
    CONSTRAINT fk_factures_entreprise
        FOREIGN KEY (entreprise_id) REFERENCES entreprises_contractees (id),
    CONSTRAINT fk_factures_cree_par
        FOREIGN KEY (cree_par_utilisateur_id) REFERENCES utilisateurs (id),
    CHECK (periode_facturation_fin >= periode_facturation_debut),
    CHECK (statut IN ('DRAFT', 'ISSUED', 'PARTIAL', 'PAID', 'VOID')),
    CHECK (montant_ht >= 0),
    CHECK (montant_taxe >= 0),
    CHECK (montant_ttc >= 0),
    CHECK (montant_paye >= 0),
    CHECK (montant_restant >= 0)
);

CREATE TABLE lignes_facture (
    id BIGINT NOT NULL AUTO_INCREMENT,
    facture_id BIGINT NOT NULL,
    type_ligne VARCHAR(20) NOT NULL,
    reference_source VARCHAR(80) NULL,
    id_source BIGINT NULL,
    description VARCHAR(255) NOT NULL,
    produit_carburant_id BIGINT NULL,
    quantite DECIMAL(12, 3) NULL,
    prix_unitaire DECIMAL(12, 3) NULL,
    taux_taxe DECIMAL(6, 3) NOT NULL DEFAULT 0,
    montant_taxe DECIMAL(14, 3) NOT NULL DEFAULT 0,
    montant_ligne DECIMAL(14, 3) NOT NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_lignes_facture_facture (facture_id),
    CONSTRAINT fk_lignes_facture_facture
        FOREIGN KEY (facture_id) REFERENCES factures (id),
    CONSTRAINT fk_lignes_facture_produit
        FOREIGN KEY (produit_carburant_id) REFERENCES produits_carburant (id),
    CHECK (type_ligne IN ('VOUCHER', 'DELIVERY_NOTE', 'ADJUSTMENT')),
    CHECK (quantite IS NULL OR quantite >= 0),
    CHECK (prix_unitaire IS NULL OR prix_unitaire >= 0),
    CHECK (taux_taxe >= 0),
    CHECK (montant_taxe >= 0),
    CHECK (montant_ligne >= 0)
);

CREATE TABLE paiements_facture (
    id BIGINT NOT NULL AUTO_INCREMENT,
    facture_id BIGINT NOT NULL,
    reference_paiement VARCHAR(80) NOT NULL,
    date_paiement DATE NOT NULL,
    montant DECIMAL(14, 3) NOT NULL,
    mode_paiement VARCHAR(30) NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    recu_par_utilisateur_id BIGINT NULL,
    notes VARCHAR(255) NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_paiements_facture_reference (reference_paiement),
    KEY idx_paiements_facture_facture_date (facture_id, date_paiement),
    CONSTRAINT fk_paiements_facture_facture
        FOREIGN KEY (facture_id) REFERENCES factures (id),
    CONSTRAINT fk_paiements_facture_recu_par
        FOREIGN KEY (recu_par_utilisateur_id) REFERENCES utilisateurs (id),
    CHECK (montant > 0),
    CHECK (mode_paiement IN ('CASH', 'BANK_TRANSFER', 'CARD', 'CHECK', 'OTHER')),
    CHECK (statut IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED'))
);

-- ============================================================================
-- Audit / reporting / migration
-- ============================================================================

CREATE TABLE journaux_audit (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NULL,
    utilisateur_id BIGINT NULL,
    type_evenement VARCHAR(40) NOT NULL,
    nom_entite VARCHAR(80) NOT NULL,
    id_entite BIGINT NULL,
    action VARCHAR(40) NOT NULL,
    adresse_ip VARCHAR(45) NULL,
    agent_utilisateur VARCHAR(255) NULL,
    anciennes_valeurs_json LONGTEXT NULL,
    nouvelles_valeurs_json LONGTEXT NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_journaux_audit_societe_temps (societe_id, cree_le),
    KEY idx_journaux_audit_entite (nom_entite, id_entite),
    KEY idx_journaux_audit_utilisateur_temps (utilisateur_id, cree_le),
    CONSTRAINT fk_journaux_audit_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_journaux_audit_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id)
);

CREATE TABLE metriques_journalieres (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    date_metrique DATE NOT NULL,
    etablissement_id BIGINT NULL,
    entreprise_id BIGINT NULL,
    nb_bons_emis INT NOT NULL DEFAULT 0,
    nb_bons_consommes INT NOT NULL DEFAULT 0,
    quantite_emise DECIMAL(14, 3) NOT NULL DEFAULT 0,
    quantite_consommee DECIMAL(14, 3) NOT NULL DEFAULT 0,
    montant_total_facture DECIMAL(14, 3) NOT NULL DEFAULT 0,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    modifie_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_metriques_journalieres_scope (societe_id, date_metrique, etablissement_id, entreprise_id),
    KEY idx_metriques_journalieres_date (date_metrique),
    CONSTRAINT fk_metriques_journalieres_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_metriques_journalieres_etablissement
        FOREIGN KEY (etablissement_id) REFERENCES etablissements (id),
    CONSTRAINT fk_metriques_journalieres_entreprise
        FOREIGN KEY (entreprise_id) REFERENCES entreprises_contractees (id),
    CHECK (nb_bons_emis >= 0),
    CHECK (nb_bons_consommes >= 0),
    CHECK (quantite_emise >= 0),
    CHECK (quantite_consommee >= 0),
    CHECK (montant_total_facture >= 0)
);

CREATE TABLE lots_migration (
    id BIGINT NOT NULL AUTO_INCREMENT,
    societe_id BIGINT NOT NULL,
    systeme_source VARCHAR(80) NOT NULL,
    version_schema_source VARCHAR(40) NULL,
    annee_debut_migration INT NOT NULL,
    annee_fin_migration INT NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    demarre_le DATETIME(3) NULL,
    termine_le DATETIME(3) NULL,
    cree_par_utilisateur_id BIGINT NULL,
    notes VARCHAR(500) NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_lots_migration_societe_statut (societe_id, statut),
    CONSTRAINT fk_lots_migration_societe
        FOREIGN KEY (societe_id) REFERENCES societes (id),
    CONSTRAINT fk_lots_migration_cree_par
        FOREIGN KEY (cree_par_utilisateur_id) REFERENCES utilisateurs (id),
    CHECK (annee_debut_migration >= 2000),
    CHECK (annee_fin_migration >= annee_debut_migration),
    CHECK (statut IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED'))
);

CREATE TABLE lignes_lot_migration (
    id BIGINT NOT NULL AUTO_INCREMENT,
    lot_migration_id BIGINT NOT NULL,
    table_source VARCHAR(80) NOT NULL,
    cle_primaire_source VARCHAR(120) NOT NULL,
    table_cible VARCHAR(80) NULL,
    cle_primaire_cible VARCHAR(120) NULL,
    statut_ligne VARCHAR(20) NOT NULL,
    message_erreur VARCHAR(255) NULL,
    migre_le DATETIME(3) NULL,
    cree_le DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_lignes_lot_migration_lot_statut (lot_migration_id, statut_ligne),
    CONSTRAINT fk_lignes_lot_migration_lot
        FOREIGN KEY (lot_migration_id) REFERENCES lots_migration (id),
    CHECK (statut_ligne IN ('PENDING', 'MIGRATED', 'SKIPPED', 'FAILED'))
);

-- ============================================================================
-- Vues de reporting
-- ============================================================================

CREATE OR REPLACE VIEW vw_soldes_bons AS
SELECT
    v.id AS bon_carburant_id,
    v.societe_id,
    v.reference_bon,
    v.reference_qr,
    v.statut,
    v.valide_de,
    v.valide_jusqua,
    v.quantite_autorisee,
    v.quantite_consommee,
    v.quantite_restante,
    c.code_entreprise,
    c.raison_sociale AS nom_entreprise,
    fp.code_produit AS code_produit_carburant,
    fp.libelle AS libelle_produit_carburant
FROM bons_carburant v
JOIN entreprises_contractees c ON c.id = v.entreprise_id
JOIN produits_carburant fp ON fp.id = v.produit_carburant_id;

CREATE OR REPLACE VIEW vw_consommation_mensuelle_entreprise AS
SELECT
    v.societe_id,
    v.entreprise_id,
    DATE_FORMAT(t.evenement_le, '%Y-%m-01') AS mois_debut,
    SUM(CASE WHEN t.type_evenement = 'CONSUMPTION' THEN t.delta_quantite ELSE 0 END) AS quantite_consommee,
    COUNT(DISTINCT CASE WHEN t.type_evenement = 'CONSUMPTION' THEN v.id ELSE NULL END) AS nb_bons_consommes
FROM transactions_bon t
JOIN bons_carburant v ON v.id = t.bon_carburant_id
GROUP BY
    v.societe_id,
    v.entreprise_id,
    DATE_FORMAT(t.evenement_le, '%Y-%m-01');

-- ============================================================================
-- Donnees de reference
-- ============================================================================

INSERT INTO roles (code_role, nom_role, est_systeme) VALUES
('ADMIN', 'Administrator', TRUE),
('MANAGER', 'Manager', TRUE),
('AGENT', 'Station Agent', TRUE),
('ACCOUNTANT', 'Accountant', TRUE);

INSERT INTO permissions (code_permission, description) VALUES
('USERS_MANAGE', 'Create, update and disable users'),
('COMPANIES_MANAGE', 'Manage contracted companies'),
('VEHICLES_MANAGE', 'Manage vehicles'),
('CONTRACTS_MANAGE', 'Manage contracts and entitlements'),
('VOUCHERS_ISSUE', 'Issue and print vouchers'),
('VOUCHERS_SCAN', 'Scan and validate vouchers'),
('VOUCHERS_REGENERATE', 'Regenerate defective vouchers'),
('BILLING_MANAGE', 'Manage invoices and delivery notes'),
('REPORTS_VIEW', 'View dashboards and reports'),
('MIGRATION_RUN', 'Run historical migration jobs');

INSERT INTO roles_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p
WHERE
    (r.code_role = 'ADMIN')
    OR (r.code_role = 'MANAGER' AND p.code_permission IN (
        'COMPANIES_MANAGE', 'VEHICLES_MANAGE', 'CONTRACTS_MANAGE',
        'VOUCHERS_ISSUE', 'VOUCHERS_SCAN', 'VOUCHERS_REGENERATE',
        'REPORTS_VIEW'
    ))
    OR (r.code_role = 'AGENT' AND p.code_permission IN (
        'VOUCHERS_SCAN', 'VOUCHERS_ISSUE', 'REPORTS_VIEW'
    ))
    OR (r.code_role = 'ACCOUNTANT' AND p.code_permission IN (
        'BILLING_MANAGE', 'REPORTS_VIEW'
    ));

INSERT INTO produits_carburant (code_produit, libelle, unite, actif) VALUES
('DIESEL', 'Diesel', 'LITER', TRUE),
('GASOLINE_95', 'Gasoline 95', 'LITER', TRUE),
('GASOLINE_98', 'Gasoline 98', 'LITER', TRUE),
('GPL', 'GPL', 'LITER', TRUE);

INSERT INTO societes (
    code_societe,
    raison_sociale,
    nom_commercial,
    matricule_fiscal,
    statut,
    langue_defaut,
    fuseau_horaire
) VALUES (
    'WTM-2SM',
    'Web Technology Masters - Societe de Services Mghira',
    'WTM-2SM',
    'TBD',
    'ACTIVE',
    'fr',
    'Africa/Tunis'
);

INSERT INTO etablissements (
    societe_id,
    code_etablissement,
    nom,
    type_etablissement,
    adresse_ligne_1,
    ville,
    gouvernorat,
    code_pays,
    actif
)
SELECT
    s.id,
    'MGHIRA-001',
    'Station Mghira Principale',
    'STATION',
    'Mghira 1 Fouchana',
    'Fouchana',
    'Ben Arous',
    'TN',
    TRUE
FROM societes s
WHERE s.code_societe = 'WTM-2SM';

-- ============================================================================
-- Fin du schema
-- ============================================================================
