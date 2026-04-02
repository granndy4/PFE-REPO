package com.wtm.fuelvoucher.contrat;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.wtm.fuelvoucher.audit.AuditJournalService;
import com.wtm.fuelvoucher.auth.UserAccountRepository;
import com.wtm.fuelvoucher.contrat.dto.ContratRequest;
import com.wtm.fuelvoucher.contrat.dto.ContratResponse;
import com.wtm.fuelvoucher.entreprise.EntrepriseContractee;
import com.wtm.fuelvoucher.entreprise.EntrepriseContracteeRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContratService {

    private final ContratRepository contratRepository;
    private final EntrepriseContracteeRepository entrepriseContracteeRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuditJournalService auditJournalService;

    public ContratService(ContratRepository contratRepository,
                          EntrepriseContracteeRepository entrepriseContracteeRepository,
                          UserAccountRepository userAccountRepository,
                          AuditJournalService auditJournalService) {
        this.contratRepository = contratRepository;
        this.entrepriseContracteeRepository = entrepriseContracteeRepository;
        this.userAccountRepository = userAccountRepository;
        this.auditJournalService = auditJournalService;
    }

    @Transactional(readOnly = true)
    public Page<ContratResponse> list(Long societeId,
                                      Long entrepriseId,
                                      ContratStatut statut,
                                      String search,
                                      Pageable pageable) {
        String normalizedSearch = normalizeSearch(search);
        return contratRepository.search(societeId, entrepriseId, statut, normalizedSearch, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ContratResponse create(ContratRequest request, String username) {
        String numeroContrat = normalizeNumeroContrat(request.numeroContrat());
        if (contratRepository.existsBySocieteIdAndNumeroContrat(request.societeId(), numeroContrat)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Contract number already exists in this societe");
        }

        EntrepriseContractee entreprise = getEntrepriseOrThrow(request.entrepriseId());
        validateSocieteConsistency(request.societeId(), entreprise);
        validateDateRange(request.dateDebut(), request.dateFin());

        Contrat contrat = new Contrat();
        applyRequest(contrat, request, numeroContrat);
        contrat.setCreeParUtilisateurId(resolveUtilisateurId(username));

        Contrat saved = contratRepository.save(contrat);
        auditJournalService.enregistrer(
                "REFERENTIEL_CONTRAT",
                "contrats",
                saved.getId(),
                "CREATE",
                saved.getSocieteId(),
                username,
                null,
                toAuditPayload(saved));
        return toResponse(saved);
    }

    @Transactional
    public ContratResponse update(Long id, ContratRequest request, String username) {
        Contrat contrat = findByIdOrThrow(id);
        Map<String, Object> anciennesValeurs = toAuditPayload(contrat);
        String numeroContrat = normalizeNumeroContrat(request.numeroContrat());

        if (contratRepository.existsBySocieteIdAndNumeroContratAndIdNot(request.societeId(), numeroContrat, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Contract number already exists in this societe");
        }

        EntrepriseContractee entreprise = getEntrepriseOrThrow(request.entrepriseId());
        validateSocieteConsistency(request.societeId(), entreprise);
        validateDateRange(request.dateDebut(), request.dateFin());

        applyRequest(contrat, request, numeroContrat);

        Contrat updated = contratRepository.save(contrat);
        auditJournalService.enregistrer(
                "REFERENTIEL_CONTRAT",
                "contrats",
                updated.getId(),
                "UPDATE",
                updated.getSocieteId(),
                username,
                anciennesValeurs,
                toAuditPayload(updated));
        return toResponse(updated);
    }

    @Transactional
    public ContratResponse updateStatut(Long id, ContratStatut statut, String username) {
        Contrat contrat = findByIdOrThrow(id);
        ContratStatut ancienStatut = contrat.getStatut();

        contrat.setStatut(statut);
        Contrat updated = contratRepository.save(contrat);

        auditJournalService.enregistrer(
                "REFERENTIEL_CONTRAT",
                "contrats",
                updated.getId(),
                "STATUS_CHANGE",
                updated.getSocieteId(),
                username,
                Map.of("statut", ancienStatut.name()),
                Map.of("statut", updated.getStatut().name()));
        return toResponse(updated);
    }

    private Contrat findByIdOrThrow(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrat not found"));
    }

    private EntrepriseContractee getEntrepriseOrThrow(Long entrepriseId) {
        return entrepriseContracteeRepository.findById(entrepriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entreprise not found"));
    }

    private void validateSocieteConsistency(Long societeId, EntrepriseContractee entreprise) {
        if (!entreprise.getSocieteId().equals(societeId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "societeId must match entreprise societe");
        }
    }

    private void validateDateRange(java.time.LocalDate dateDebut, java.time.LocalDate dateFin) {
        if (dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dateFin must be on or after dateDebut");
        }
    }

    private void applyRequest(Contrat contrat, ContratRequest request, String numeroContrat) {
        contrat.setSocieteId(request.societeId());
        contrat.setEntrepriseId(request.entrepriseId());
        contrat.setNumeroContrat(numeroContrat);
        contrat.setDateDebut(request.dateDebut());
        contrat.setDateFin(request.dateFin());
        contrat.setCodeDevise(normalizeCodeDevise(request.codeDevise()));
        contrat.setDelaiPaiementJours(request.delaiPaiementJours() == null ? 30 : request.delaiPaiementJours());
        contrat.setMontantMaxMensuel(request.montantMaxMensuel());
        contrat.setStatut(request.statut() == null ? ContratStatut.ACTIVE : request.statut());
        contrat.setSigneLe(request.signeLe());
        contrat.setNotes(normalizeNullable(request.notes()));
    }

    private ContratResponse toResponse(Contrat contrat) {
        return new ContratResponse(
                contrat.getId(),
                contrat.getSocieteId(),
                contrat.getEntrepriseId(),
                contrat.getNumeroContrat(),
                contrat.getDateDebut(),
                contrat.getDateFin(),
                contrat.getCodeDevise(),
                contrat.getDelaiPaiementJours(),
                contrat.getMontantMaxMensuel(),
                contrat.getStatut(),
                contrat.getSigneLe(),
                contrat.getNotes(),
                contrat.getCreeParUtilisateurId(),
                contrat.getCreeLe(),
                contrat.getModifieLe());
    }

    private String normalizeNumeroContrat(String numeroContrat) {
        return numeroContrat.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCodeDevise(String codeDevise) {
        if (codeDevise == null || codeDevise.isBlank()) {
            return "TND";
        }

        String normalized = codeDevise.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() != 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "codeDevise must contain 3 letters");
        }
        return normalized;
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

    private Long resolveUtilisateurId(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        String normalizedEmail = username.trim().toLowerCase(Locale.ROOT);
        return userAccountRepository.findByEmail(normalizedEmail)
                .map(user -> user.getId())
                .orElse(null);
    }

    private Map<String, Object> toAuditPayload(Contrat contrat) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", contrat.getId());
        payload.put("societeId", contrat.getSocieteId());
        payload.put("entrepriseId", contrat.getEntrepriseId());
        payload.put("numeroContrat", contrat.getNumeroContrat());
        payload.put("dateDebut", contrat.getDateDebut());
        payload.put("dateFin", contrat.getDateFin());
        payload.put("codeDevise", contrat.getCodeDevise());
        payload.put("delaiPaiementJours", contrat.getDelaiPaiementJours());
        payload.put("montantMaxMensuel", contrat.getMontantMaxMensuel());
        payload.put("statut", contrat.getStatut().name());
        payload.put("signeLe", contrat.getSigneLe());
        payload.put("notes", contrat.getNotes());
        payload.put("creeParUtilisateurId", contrat.getCreeParUtilisateurId());
        return payload;
    }
}