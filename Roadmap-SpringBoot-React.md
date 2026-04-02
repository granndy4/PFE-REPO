# Roadmap Projet - Gestion des Bons de Carburants

## 1. Vision produit
Construire une application web multi-societes pour gerer le cycle complet des bons de carburant:
- administration des societes, vehicules, personnel et contrats,
- generation et validation des bons via QR code,
- suivi de consommation en temps reel,
- facturation, bons de livraison, reporting et tableaux de bord,
- securite avancee, tracabilite, migration de donnees et performance (< 3 secondes).

Duree cible: 6 mois.

## 2. Stack cible (Spring Boot + ReactJS)

### Back-end
- Java 17

- Spring Boot 3
- Spring Security + JWT + RBAC
- Spring Data JPA + Hibernate
- PostgreSQL
- OpenAPI/Swagger

### Front-end
- Reactjs
- Vite
- React Router
- Redux Toolkit + RTK Query
- Tailwind CSS
- i18next (fr, ar, en)
- react-hook-form + zod
- QR scanner web (html5-qrcode)
- Charts (Recharts ou ECharts)

### DevOps
- Docker / Docker Compose
- GitHub Actions (CI)
- Environnements: Dev, Preprod, Prod
- Monitoring: Prometheus + Grafana (ou equivalent)
- Logs centralises: ELK ou Loki

## 3. Architecture fonctionnelle proposee

### Microservices (version initiale)
- auth-service: authentification, autorisation, gestion utilisateurs/roles.
- referential-service: entreprises, etablissements, vehicules, personnel, contrats.
- voucher-service: bons, QR code, scan, validation, solde, regeneration.
- billing-service: factures, bons de livraison.
- reporting-service: rapports, statistiques, tableaux de bord, historique.
- migration-service: import des donnees historiques.

### Composants transverses
- API Gateway
- Service de notification (email/SMS optionnel)
- Audit service (journalisation des actions utilisateurs)

### Base de donnees
- PostgreSQL par service (isolation de donnees)
- Evenements asynchrones entre services si necessaire (RabbitMQ/Kafka optionnel)

## 4. Plan de livraison sur 6 mois (12 sprints de 2 semaines)

## Mois 1 - Cadrage et fondations (Sprints 1-2)
Objectif: securiser la base technique et le backlog.

Livrables:
- cadrage detaille des parcours (admin, agent station, responsable),
- modelisation du domaine (ERD + contrats API),
- setup repo, CI/CD, environnements,
- socle Auth (JWT, RBAC),
- socle React (layout, routing, i18n, design system),
- charte UX/UI alignee avec identite WTM.

Definition of Done:
- login/logout fonctionnel,
- roles de base actifs,
- pipeline CI vert sur front/back,
- documentation technique initiale.

## Mois 2 - Referentiel metier (Sprints 3-4)
Objectif: couvrir la gestion des entites principales.

Livrables:
- CRUD entreprises contractees,
- CRUD vehicules,
- CRUD personnel affecte,
- CRUD contrats client,
- controles de validite metier,
- ecrans de recherche, filtres et pagination.

Definition of Done:
- tests unitaires + integration pour tous les CRUD,
- gestion des droits par role,
- audit des operations creation/modification/suppression.

## Mois 3 - Cycle des bons et QR code (Sprints 5-6)
Objectif: coeur metier operationnel.

Livrables:
- generation des bons carburant avec QR,
- impression/export des bons,
- scan et validation des references QR,
- decrement automatique du solde carburant,
- regeneration des bons defectueux mais valides.

Definition of Done:
- anti-double-utilisation d un bon,
- gestion des erreurs de scan,
- performance validation QR < 2 secondes mediane.

## Mois 4 - Facturation, historique et reporting (Sprints 7-8)
Objectif: cloturer le flux administratif.

Livrables:
- module facturation,
- gestion des bons de livraison,
- historique par periode et par entreprise,
- rapports consommation,
- tableaux de bord KPI (volume, montant, anomalies, consommation par client).

Definition of Done:
- exports PDF/Excel,
- filtres multicriteres,
- coherence des chiffres entre operationnel et reporting.

## Mois 5 - Migration, securite, qualite et performance (Sprints 9-10)
Objectif: rendre la solution deployable en production.

Livrables:
- module de migration (annee de depart -> annee courante),
- jeux de reprise + validation des donnees migrees,
- durcissement securite (OWASP top 10),
- sauvegarde et archivage automatiques,
- optimisation requetes, cache et charge,
- compatibilite navigateurs + responsive complet.

Definition of Done:
- tests de performance (temps de reponse < 3 secondes pour endpoints critiques),
- tests de securite et correction des failles,
- scripts d exploitation (backup, restore, rotation logs).

## Mois 6 - Pilote, recette et mise en production (Sprints 11-12)
Objectif: livrer une version stable et exploitable.

Livrables:
- recette metier complete avec client pilote,
- correction bugs bloquants,
- documentation utilisateur + admin,
- formation des utilisateurs,
- plan de mise en production,
- hypercare post go-live (2 a 4 semaines).

Definition of Done:
- PV de recette signe,
- runbook production valide,
- backlog V2 etabli (IA, BI avancee, interfacage temps reel approfondi).

## 5. MVP recommande (a livrer fin Mois 3)
Fonctions minimales pour valeur immediate:
- authentification + roles,
- gestion entreprises/vehicules/personnel/contrats,
- generation bon + QR,
- scan et validation bon,
- mise a jour du solde,
- historique basique et rapport simple.

## 6. Backlog technique transversal
- Multi-langues: fr, ar, en (i18n cote front + gestion des labels metier).
- Tracabilite: journal utilisateur pour toutes les actions sensibles.
- Observabilite: logs, metriques, alerting.
- Gouvernance API: versioning, conventions erreurs, timeout/retry.
- Qualite code: Sonar, couverture de tests cible >= 70% sur modules critiques.

## 7. Equipe type
- 1 Tech Lead (Spring Boot + architecture)
- 1 Dev Back-end
- 1 Dev Front-end React
- 1 QA/Test
- 1 DevOps part-time
- 1 Product Owner metier (cote station/client)

## 8. Rituels de pilotage
- Daily 15 min
- Weekly revue d avancement (samedi matin selon cahier des charges)
- Sprint Review toutes les 2 semaines
- Sprint Retro toutes les 2 semaines
- Suivi KPI: velocite, taux bugs, lead time, taux de reussite CI/CD

## 9. Risques et mitigation
- Risque: ambiguite des regles de gestion des bons.
  Mitigation: atelier metier et tests d acceptation des Sprints 1-2.

- Risque: qualite des donnees historiques pour migration.
  Mitigation: maquette migration des Mois 2-3 + nettoyage en amont.

- Risque: performance lors des pics de scan.
  Mitigation: tests de charge des Mois 4-5 + cache + index DB.

- Risque: resistance au changement utilisateurs.
  Mitigation: formation progressive + guide d utilisation + support pilote.

## 10. Jalons de validation
- J1 (fin Mois 1): architecture validee + socle securite pret.
- J2 (fin Mois 3): MVP operationnel en preprod.
- J3 (fin Mois 5): release candidate securisee et performante.
- J4 (fin Mois 6): go-live + hypercare.

## 11. Extension V2 (apres go-live)
- Module IA d aide a la decision:
  - prediction de consommation par client,
  - detection d anomalies/fraudes,
  - recommandations de seuils et quotas.
- Integration plus poussee temps reel avec systemes station.
- Mobile app dediee (React Native) pour scan terrain.
