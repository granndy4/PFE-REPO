package com.wtm.fuelvoucher.Services;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.wtm.fuelvoucher.Entities.EntrepriseContractee;
import com.wtm.fuelvoucher.Dtos.EntrepriseRequest;
import com.wtm.fuelvoucher.Dtos.EntrepriseResponse;
import com.wtm.fuelvoucher.Repositories.EntrepriseContracteeRepository;
import com.wtm.fuelvoucher.Enums.EntrepriseStatut;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EntrepriseService {

    private final EntrepriseContracteeRepository entrepriseContracteeRepository;
    private final AuditJournalService auditJournalService;

    public EntrepriseService(EntrepriseContracteeRepository entrepriseContracteeRepository,
                             AuditJournalService auditJournalService) {
        this.entrepriseContracteeRepository = entrepriseContracteeRepository;
        this.auditJournalService = auditJournalService;
    }

    @Transactional(readOnly = true)
    public Page<EntrepriseResponse> list(Long societeId,
                                         EntrepriseStatut statut,
                                         String search,
                                         Pageable pageable) {
        String normalizedSearch = normalizeSearch(search);
        return entrepriseContracteeRepository
                .search(societeId, statut, normalizedSearch, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public EntrepriseResponse create(EntrepriseRequest request, String username) {
        String codeEntreprise = normalizeCode(request.codeEntreprise());
        if (entrepriseContracteeRepository.existsBySocieteIdAndCodeEntreprise(request.societeId(), codeEntreprise)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code entreprise already exists in this societe");
        }

        EntrepriseContractee entreprise = new EntrepriseContractee();
        applyRequest(entreprise, request, codeEntreprise);

        EntrepriseContractee saved = entrepriseContracteeRepository.save(entreprise);
        auditJournalService.enregistrer(
            "REFERENTIEL_ENTREPRISE",
            "entreprises_contractees",
            saved.getId(),
            "CREATE",
            saved.getSocieteId(),
            username,
            null,
            toAuditPayload(saved));
        return toResponse(saved);
    }

    @Transactional
        public EntrepriseResponse update(Long id, EntrepriseRequest request, String username) {
        EntrepriseContractee entreprise = findByIdOrThrow(id);
        Map<String, Object> anciennesValeurs = toAuditPayload(entreprise);
        String codeEntreprise = normalizeCode(request.codeEntreprise());

        boolean duplicate = entrepriseContracteeRepository.existsBySocieteIdAndCodeEntrepriseAndIdNot(
                request.societeId(), codeEntreprise, id);
        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code entreprise already exists in this societe");
        }

        applyRequest(entreprise, request, codeEntreprise);

        EntrepriseContractee updated = entrepriseContracteeRepository.save(entreprise);
        auditJournalService.enregistrer(
            "REFERENTIEL_ENTREPRISE",
            "entreprises_contractees",
            updated.getId(),
            "UPDATE",
            updated.getSocieteId(),
            username,
            anciennesValeurs,
            toAuditPayload(updated));
        return toResponse(updated);
    }

    @Transactional
        public EntrepriseResponse updateStatut(Long id, EntrepriseStatut statut, String username) {
        EntrepriseContractee entreprise = findByIdOrThrow(id);
        EntrepriseStatut ancienStatut = entreprise.getStatut();
        entreprise.setStatut(statut);

        EntrepriseContractee updated = entrepriseContracteeRepository.save(entreprise);
        auditJournalService.enregistrer(
            "REFERENTIEL_ENTREPRISE",
            "entreprises_contractees",
            updated.getId(),
            "STATUS_CHANGE",
            updated.getSocieteId(),
            username,
            Map.of("statut", ancienStatut.name()),
            Map.of("statut", updated.getStatut().name()));
        return toResponse(updated);
    }

    private EntrepriseContractee findByIdOrThrow(Long id) {
        return entrepriseContracteeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entreprise not found"));
    }

    private void applyRequest(EntrepriseContractee entreprise, EntrepriseRequest request, String codeEntreprise) {
        entreprise.setSocieteId(request.societeId());
        entreprise.setCodeEntreprise(codeEntreprise);
        entreprise.setRaisonSociale(request.raisonSociale().trim());
        entreprise.setNomCourt(normalizeNullable(request.nomCourt()));
        entreprise.setMatriculeFiscal(normalizeNullable(request.matriculeFiscal()));
        entreprise.setAdresseFacturation(normalizeNullable(request.adresseFacturation()));
        entreprise.setNomContact(normalizeNullable(request.nomContact()));
        entreprise.setEmailContact(normalizeNullable(request.emailContact()));
        entreprise.setTelephoneContact(normalizeNullable(request.telephoneContact()));
        entreprise.setStatut(request.statut() == null ? EntrepriseStatut.ACTIVE : request.statut());
    }

    private EntrepriseResponse toResponse(EntrepriseContractee entreprise) {
        return new EntrepriseResponse(
                entreprise.getId(),
                entreprise.getSocieteId(),
                entreprise.getCodeEntreprise(),
                entreprise.getRaisonSociale(),
                entreprise.getNomCourt(),
                entreprise.getMatriculeFiscal(),
                entreprise.getAdresseFacturation(),
                entreprise.getNomContact(),
                entreprise.getEmailContact(),
                entreprise.getTelephoneContact(),
                entreprise.getStatut(),
                entreprise.getCreeLe(),
                entreprise.getModifieLe());
    }

    private String normalizeCode(String codeEntreprise) {
        return codeEntreprise.trim().toUpperCase(Locale.ROOT);
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

    private Map<String, Object> toAuditPayload(EntrepriseContractee entreprise) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", entreprise.getId());
        payload.put("societeId", entreprise.getSocieteId());
        payload.put("codeEntreprise", entreprise.getCodeEntreprise());
        payload.put("raisonSociale", entreprise.getRaisonSociale());
        payload.put("nomCourt", entreprise.getNomCourt());
        payload.put("matriculeFiscal", entreprise.getMatriculeFiscal());
        payload.put("adresseFacturation", entreprise.getAdresseFacturation());
        payload.put("nomContact", entreprise.getNomContact());
        payload.put("emailContact", entreprise.getEmailContact());
        payload.put("telephoneContact", entreprise.getTelephoneContact());
        payload.put("statut", entreprise.getStatut().name());
        return payload;
    }
}




