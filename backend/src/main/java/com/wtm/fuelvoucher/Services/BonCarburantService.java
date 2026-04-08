package com.wtm.fuelvoucher.Services;

import com.wtm.fuelvoucher.Dtos.BonConsumeRequest;
import com.wtm.fuelvoucher.Dtos.BonCreateRequest;
import com.wtm.fuelvoucher.Dtos.BonRegenerateRequest;
import com.wtm.fuelvoucher.Dtos.BonResponse;
import com.wtm.fuelvoucher.Dtos.BonValidationResponse;
import com.wtm.fuelvoucher.Entities.BonCarburant;
import com.wtm.fuelvoucher.Entities.BonConsommation;
import com.wtm.fuelvoucher.Entities.Contrat;
import com.wtm.fuelvoucher.Entities.EmployeEntreprise;
import com.wtm.fuelvoucher.Entities.EntrepriseContractee;
import com.wtm.fuelvoucher.Entities.Vehicule;
import com.wtm.fuelvoucher.Enums.BonStatut;
import com.wtm.fuelvoucher.Repositories.BonCarburantRepository;
import com.wtm.fuelvoucher.Repositories.BonConsommationRepository;
import com.wtm.fuelvoucher.Repositories.ContratRepository;
import com.wtm.fuelvoucher.Repositories.EmployeEntrepriseRepository;
import com.wtm.fuelvoucher.Repositories.EntrepriseContracteeRepository;
import com.wtm.fuelvoucher.Repositories.UserAccountRepository;
import com.wtm.fuelvoucher.Repositories.VehiculeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BonCarburantService {

    private final BonCarburantRepository bonCarburantRepository;
    private final BonConsommationRepository bonConsommationRepository;
    private final EntrepriseContracteeRepository entrepriseContracteeRepository;
    private final VehiculeRepository vehiculeRepository;
    private final EmployeEntrepriseRepository employeEntrepriseRepository;
    private final ContratRepository contratRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuditJournalService auditJournalService;

    public BonCarburantService(BonCarburantRepository bonCarburantRepository,
                               BonConsommationRepository bonConsommationRepository,
                               EntrepriseContracteeRepository entrepriseContracteeRepository,
                               VehiculeRepository vehiculeRepository,
                               EmployeEntrepriseRepository employeEntrepriseRepository,
                               ContratRepository contratRepository,
                               UserAccountRepository userAccountRepository,
                               AuditJournalService auditJournalService) {
        this.bonCarburantRepository = bonCarburantRepository;
        this.bonConsommationRepository = bonConsommationRepository;
        this.entrepriseContracteeRepository = entrepriseContracteeRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.employeEntrepriseRepository = employeEntrepriseRepository;
        this.contratRepository = contratRepository;
        this.userAccountRepository = userAccountRepository;
        this.auditJournalService = auditJournalService;
    }

    @Transactional(readOnly = true)
    public Page<BonResponse> list(Long societeId,
                                  Long entrepriseId,
                                  Long vehiculeId,
                                  BonStatut statut,
                                  String search,
                                  Pageable pageable) {
        String normalizedSearch = normalizeSearch(search);
        return bonCarburantRepository.search(societeId, entrepriseId, vehiculeId, statut, normalizedSearch, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public BonResponse create(BonCreateRequest request, String username) {
        EntrepriseContractee entreprise = getEntrepriseOrThrow(request.entrepriseId());
        Vehicule vehicule = getVehiculeOrThrow(request.vehiculeId());

        validateSocieteConsistency(request.societeId(), entreprise.getSocieteId(), "Entreprise") ;
        validateSocieteConsistency(request.societeId(), vehicule.getSocieteId(), "Vehicule");
        if (!vehicule.getEntrepriseId().equals(request.entrepriseId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicule does not belong to entreprise");
        }

        if (request.employeId() != null) {
            EmployeEntreprise employe = getEmployeOrThrow(request.employeId());
            validateSocieteConsistency(request.societeId(), employe.getSocieteId(), "Employe");
            if (!employe.getEntrepriseId().equals(request.entrepriseId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employe does not belong to entreprise");
            }
        }

        if (request.contratId() != null) {
            Contrat contrat = getContratOrThrow(request.contratId());
            validateSocieteConsistency(request.societeId(), contrat.getSocieteId(), "Contrat");
            if (!contrat.getEntrepriseId().equals(request.entrepriseId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contrat does not belong to entreprise");
            }
        }

        ensureTransactionReferenceIsUnique(request.referenceTransactionInitiale());
        BigDecimal quantite = normalizePositiveQuantity(request.quantiteInitialeLitres(), "Initial quantity");

        BonCarburant bon = new BonCarburant();
        bon.setSocieteId(request.societeId());
        bon.setEntrepriseId(request.entrepriseId());
        bon.setContratId(request.contratId());
        bon.setVehiculeId(request.vehiculeId());
        bon.setEmployeId(request.employeId());
        bon.setReferenceBon(resolveReferenceBon(request.referenceBon()));
        bon.setQrNonce(UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase(Locale.ROOT));
        bon.setQuantiteInitialeLitres(quantite);
        bon.setSoldeLitres(quantite);
        bon.setStatut(BonStatut.ISSUED);
        bon.setCreeParUtilisateurId(resolveUtilisateurId(username));
        bon.setQrCodePayload(buildQrPayload(bon));

        BonCarburant saved = bonCarburantRepository.save(bon);
        recordConsumption(saved.getId(), quantite, request.referenceTransactionInitiale(), resolveUtilisateurId(username), request.notes());

        auditJournalService.enregistrer(
                "BON_CARBURANT",
                "bons_carburant",
                saved.getId(),
                "CREATE",
                saved.getSocieteId(),
                username,
                null,
                toAuditPayload(saved));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BonValidationResponse validateByReference(String referenceBon) {
        String normalizedReference = normalizeReference(referenceBon);
        BonCarburant bon = bonCarburantRepository.findByReferenceBon(normalizedReference)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bon not found"));

        if (bon.getStatut() == BonStatut.CANCELLED || bon.getStatut() == BonStatut.REGENERATED) {
            return new BonValidationResponse(
                    false,
                    "Bon is not valid for consumption",
                    bon.getId(),
                    bon.getReferenceBon(),
                    bon.getStatut(),
                    bon.getSoldeLitres(),
                    bon.getQrCodePayload());
        }

        if (bon.getSoldeLitres().compareTo(BigDecimal.ZERO) <= 0 || bon.getStatut() == BonStatut.CONSUMED) {
            return new BonValidationResponse(
                    false,
                    "Bon is already fully consumed",
                    bon.getId(),
                    bon.getReferenceBon(),
                    BonStatut.CONSUMED,
                    bon.getSoldeLitres(),
                    bon.getQrCodePayload());
        }

        return new BonValidationResponse(
                true,
                "Bon is valid",
                bon.getId(),
                bon.getReferenceBon(),
                bon.getStatut(),
                bon.getSoldeLitres(),
                bon.getQrCodePayload());
    }

    @Transactional
    public BonResponse consume(Long id, BonConsumeRequest request, String username) {
        BonCarburant bon = findBonOrThrow(id);
        BigDecimal quantite = normalizePositiveQuantity(request.quantiteLitres(), "Consumption quantity");
        String transactionRef = normalizeTransactionReference(request.referenceTransaction());

        ensureTransactionReferenceIsUnique(transactionRef);

        if (bon.getStatut() == BonStatut.CANCELLED || bon.getStatut() == BonStatut.REGENERATED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bon is not consumable");
        }

        if (bon.getSoldeLitres().compareTo(BigDecimal.ZERO) <= 0 || bon.getStatut() == BonStatut.CONSUMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bon is already fully consumed");
        }

        if (quantite.compareTo(bon.getSoldeLitres()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Consumption exceeds bon remaining balance");
        }

        Map<String, Object> anciennesValeurs = toAuditPayload(bon);

        BigDecimal nouveauSolde = bon.getSoldeLitres().subtract(quantite).setScale(3, RoundingMode.HALF_UP);
        bon.setSoldeLitres(nouveauSolde);
        bon.setStatut(nouveauSolde.compareTo(BigDecimal.ZERO) == 0 ? BonStatut.CONSUMED : BonStatut.PARTIALLY_CONSUMED);

        BonCarburant updated = bonCarburantRepository.save(bon);
        recordConsumption(updated.getId(), quantite, transactionRef, resolveUtilisateurId(username), request.notes());

        auditJournalService.enregistrer(
                "BON_CARBURANT",
                "bons_carburant",
                updated.getId(),
                "CONSUME",
                updated.getSocieteId(),
                username,
                anciennesValeurs,
                toAuditPayload(updated));

        return toResponse(updated);
    }

    @Transactional
    public BonResponse regenerate(Long id, BonRegenerateRequest request, String username) {
        BonCarburant original = findBonOrThrow(id);

        if (original.getStatut() == BonStatut.CANCELLED || original.getStatut() == BonStatut.REGENERATED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bon cannot be regenerated");
        }

        if (original.getSoldeLitres().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bon has no remaining balance to regenerate");
        }

        Map<String, Object> anciennesValeurs = toAuditPayload(original);

        original.setDefectueux(true);
        original.setStatut(BonStatut.REGENERATED);
        BonCarburant oldUpdated = bonCarburantRepository.save(original);

        BonCarburant regenerated = new BonCarburant();
        regenerated.setSocieteId(oldUpdated.getSocieteId());
        regenerated.setEntrepriseId(oldUpdated.getEntrepriseId());
        regenerated.setContratId(oldUpdated.getContratId());
        regenerated.setVehiculeId(oldUpdated.getVehiculeId());
        regenerated.setEmployeId(oldUpdated.getEmployeId());
        regenerated.setReferenceBon(resolveReferenceBon(oldUpdated.getReferenceBon() + "-R"));
        regenerated.setQrNonce(UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase(Locale.ROOT));
        regenerated.setQuantiteInitialeLitres(oldUpdated.getSoldeLitres());
        regenerated.setSoldeLitres(oldUpdated.getSoldeLitres());
        regenerated.setStatut(BonStatut.ISSUED);
        regenerated.setBonOriginalId(oldUpdated.getId());
        regenerated.setCreeParUtilisateurId(resolveUtilisateurId(username));
        regenerated.setQrCodePayload(buildQrPayload(regenerated));

        BonCarburant savedNew = bonCarburantRepository.save(regenerated);

        auditJournalService.enregistrer(
                "BON_CARBURANT",
                "bons_carburant",
                oldUpdated.getId(),
                "REGENERATE_OLD",
                oldUpdated.getSocieteId(),
                username,
                anciennesValeurs,
                toAuditPayload(oldUpdated));

        auditJournalService.enregistrer(
                "BON_CARBURANT",
                "bons_carburant",
                savedNew.getId(),
                "REGENERATE_NEW",
                savedNew.getSocieteId(),
                username,
                Map.of("motif", normalizeNullable(request.motif())),
                toAuditPayload(savedNew));

        return toResponse(savedNew);
    }

    private BonCarburant findBonOrThrow(Long id) {
        return bonCarburantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bon not found"));
    }

    private EntrepriseContractee getEntrepriseOrThrow(Long entrepriseId) {
        return entrepriseContracteeRepository.findById(entrepriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entreprise not found"));
    }

    private Vehicule getVehiculeOrThrow(Long vehiculeId) {
        return vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicule not found"));
    }

    private EmployeEntreprise getEmployeOrThrow(Long employeId) {
        return employeEntrepriseRepository.findById(employeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employe not found"));
    }

    private Contrat getContratOrThrow(Long contratId) {
        return contratRepository.findById(contratId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contrat not found"));
    }

    private void validateSocieteConsistency(Long requestSocieteId, Long entitySocieteId, String entityName) {
        if (!requestSocieteId.equals(entitySocieteId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, entityName + " does not belong to societe");
        }
    }

    private BigDecimal normalizePositiveQuantity(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must be positive");
        }
        return value.setScale(3, RoundingMode.HALF_UP);
    }

    private String resolveReferenceBon(String providedReference) {
        String baseReference;
        if (providedReference == null || providedReference.isBlank()) {
            baseReference = "BON-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        } else {
            baseReference = normalizeReference(providedReference);
        }

        String candidate = baseReference;
        int suffix = 1;
        while (bonCarburantRepository.existsByReferenceBon(candidate)) {
            candidate = baseReference + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private String normalizeReference(String referenceBon) {
        String normalized = referenceBon == null ? "" : referenceBon.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reference bon is required");
        }
        return normalized;
    }

    private String normalizeTransactionReference(String referenceTransaction) {
        String normalized = referenceTransaction == null ? "" : referenceTransaction.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction reference is required");
        }
        return normalized;
    }

    private void ensureTransactionReferenceIsUnique(String transactionReference) {
        String normalized = normalizeTransactionReference(transactionReference);
        if (bonConsommationRepository.existsByReferenceTransaction(normalized)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transaction reference already used");
        }
    }

    private void recordConsumption(Long bonId,
                                   BigDecimal quantite,
                                   String transactionReference,
                                   Long utilisateurId,
                                   String notes) {
        BonConsommation consommation = new BonConsommation();
        consommation.setBonId(bonId);
        consommation.setQuantiteLitres(quantite);
        consommation.setReferenceTransaction(normalizeTransactionReference(transactionReference));
        consommation.setConsommeParUtilisateurId(utilisateurId);
        consommation.setNotes(normalizeNullable(notes));
        bonConsommationRepository.save(consommation);
    }

    private Long resolveUtilisateurId(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        String normalizedEmail = username.trim().toLowerCase(Locale.ROOT);
        return userAccountRepository.findByEmail(normalizedEmail)
                .map(user -> user.getId())
                .orElse(null);
    }

    private String buildQrPayload(BonCarburant bon) {
        return "FUELVOUCHER|" + bon.getReferenceBon() + "|" + bon.getQrNonce();
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return null;
        }
        String normalized = search.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private BonResponse toResponse(BonCarburant bon) {
        return new BonResponse(
                bon.getId(),
                bon.getSocieteId(),
                bon.getEntrepriseId(),
                bon.getContratId(),
                bon.getVehiculeId(),
                bon.getEmployeId(),
                bon.getReferenceBon(),
                bon.getQrCodePayload(),
                bon.getQuantiteInitialeLitres(),
                bon.getSoldeLitres(),
                bon.getStatut(),
                bon.isDefectueux(),
                bon.getBonOriginalId(),
                bon.getCreeLe(),
                bon.getModifieLe());
    }

    private Map<String, Object> toAuditPayload(BonCarburant bon) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", bon.getId());
        payload.put("societeId", bon.getSocieteId());
        payload.put("entrepriseId", bon.getEntrepriseId());
        payload.put("contratId", bon.getContratId());
        payload.put("vehiculeId", bon.getVehiculeId());
        payload.put("employeId", bon.getEmployeId());
        payload.put("referenceBon", bon.getReferenceBon());
        payload.put("soldeLitres", bon.getSoldeLitres());
        payload.put("quantiteInitialeLitres", bon.getQuantiteInitialeLitres());
        payload.put("statut", bon.getStatut().name());
        payload.put("defectueux", bon.isDefectueux());
        payload.put("bonOriginalId", bon.getBonOriginalId());
        return payload;
    }
}
