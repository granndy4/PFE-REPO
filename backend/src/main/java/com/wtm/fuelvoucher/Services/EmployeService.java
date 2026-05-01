package com.wtm.fuelvoucher.Services;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.wtm.fuelvoucher.Entities.EmployeEntreprise;
import com.wtm.fuelvoucher.Dtos.EmployeRequest;
import com.wtm.fuelvoucher.Dtos.EmployeResponse;
import com.wtm.fuelvoucher.Entities.EntrepriseContractee;
import com.wtm.fuelvoucher.Repositories.EmployeEntrepriseRepository;
import com.wtm.fuelvoucher.Repositories.EntrepriseContracteeRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeService {

    private final EmployeEntrepriseRepository employeEntrepriseRepository;
    private final EntrepriseContracteeRepository entrepriseContracteeRepository;
    private final AuditJournalService auditJournalService;

    public EmployeService(EmployeEntrepriseRepository employeEntrepriseRepository,
                          EntrepriseContracteeRepository entrepriseContracteeRepository,
                          AuditJournalService auditJournalService) {
        this.employeEntrepriseRepository = employeEntrepriseRepository;
        this.entrepriseContracteeRepository = entrepriseContracteeRepository;
        this.auditJournalService = auditJournalService;
    }

    @Transactional(readOnly = true)
    public Page<EmployeResponse> list(Long societeId,
                                      Long entrepriseId,
                                      Boolean actif,
                                      String search,
                                      Pageable pageable) {
        String normalizedSearch = normalizeSearch(search);
        return employeEntrepriseRepository.search(societeId, entrepriseId, actif, normalizedSearch, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public EmployeResponse create(EmployeRequest request, String username) {
        String codeEmploye = normalizeCodeEmploye(request.codeEmploye());
        if (employeEntrepriseRepository.existsByEntrepriseIdAndCodeEmploye(request.entrepriseId(), codeEmploye)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee code already exists in this entreprise");
        }

        EntrepriseContractee entreprise = getEntrepriseOrThrow(request.entrepriseId());
        validateSocieteConsistency(request.societeId(), entreprise);

        EmployeEntreprise employe = new EmployeEntreprise();
        applyRequest(employe, request, codeEmploye);

        EmployeEntreprise saved = employeEntrepriseRepository.save(employe);
        auditJournalService.enregistrer(
                "REFERENTIEL_EMPLOYE",
                "employes_entreprise",
                saved.getId(),
                "CREATE",
                saved.getSocieteId(),
                username,
                null,
                toAuditPayload(saved));
        return toResponse(saved);
    }

    @Transactional
    public EmployeResponse update(Long id, EmployeRequest request, String username) {
        EmployeEntreprise employe = findByIdOrThrow(id);
        Map<String, Object> anciennesValeurs = toAuditPayload(employe);
        String codeEmploye = normalizeCodeEmploye(request.codeEmploye());

        if (employeEntrepriseRepository.existsByEntrepriseIdAndCodeEmployeAndIdNot(request.entrepriseId(), codeEmploye, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee code already exists in this entreprise");
        }

        EntrepriseContractee entreprise = getEntrepriseOrThrow(request.entrepriseId());
        validateSocieteConsistency(request.societeId(), entreprise);

        applyRequest(employe, request, codeEmploye);

        EmployeEntreprise updated = employeEntrepriseRepository.save(employe);
        auditJournalService.enregistrer(
                "REFERENTIEL_EMPLOYE",
                "employes_entreprise",
                updated.getId(),
                "UPDATE",
                updated.getSocieteId(),
                username,
                anciennesValeurs,
                toAuditPayload(updated));
        return toResponse(updated);
    }

    @Transactional
    public EmployeResponse updateActif(Long id, boolean actif, String username) {
        EmployeEntreprise employe = findByIdOrThrow(id);
        boolean ancienActif = employe.isActif();

        employe.setActif(actif);
        EmployeEntreprise updated = employeEntrepriseRepository.save(employe);

        auditJournalService.enregistrer(
                "REFERENTIEL_EMPLOYE",
                "employes_entreprise",
                updated.getId(),
                "STATUS_CHANGE",
                updated.getSocieteId(),
                username,
                Map.of("actif", ancienActif),
                Map.of("actif", updated.isActif()));
        return toResponse(updated);
    }

    @Transactional
    public void delete(Long id, String username) {
        EmployeEntreprise employe = findByIdOrThrow(id);
        Map<String, Object> anciennesValeurs = toAuditPayload(employe);

        try {
            employeEntrepriseRepository.delete(employe);
            employeEntrepriseRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete employe because it is referenced by other records");
        }

        auditJournalService.enregistrer(
                "REFERENTIEL_EMPLOYE",
                "employes_entreprise",
                id,
                "DELETE",
                employe.getSocieteId(),
                username,
                anciennesValeurs,
                null);
    }

    private EmployeEntreprise findByIdOrThrow(Long id) {
        return employeEntrepriseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employe not found"));
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

    private void applyRequest(EmployeEntreprise employe, EmployeRequest request, String codeEmploye) {
        employe.setSocieteId(request.societeId());
        employe.setEntrepriseId(request.entrepriseId());
        employe.setCodeEmploye(codeEmploye);
        employe.setNomComplet(request.nomComplet().trim());
        employe.setCin(normalizeNullable(request.cin()));
        employe.setTelephone(normalizeNullable(request.telephone()));
        employe.setEmail(normalizeNullable(request.email()));
        employe.setPoste(normalizeNullable(request.poste()));
        employe.setActif(request.actif() == null ? true : request.actif());
    }

    private EmployeResponse toResponse(EmployeEntreprise employe) {
        return new EmployeResponse(
                employe.getId(),
                employe.getSocieteId(),
                employe.getEntrepriseId(),
                employe.getCodeEmploye(),
                employe.getNomComplet(),
                employe.getCin(),
                employe.getTelephone(),
                employe.getEmail(),
                employe.getPoste(),
                employe.isActif(),
                employe.getCreeLe(),
                employe.getModifieLe());
    }

    private String normalizeCodeEmploye(String codeEmploye) {
        return codeEmploye.trim().toUpperCase(Locale.ROOT);
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

    private Map<String, Object> toAuditPayload(EmployeEntreprise employe) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", employe.getId());
        payload.put("societeId", employe.getSocieteId());
        payload.put("entrepriseId", employe.getEntrepriseId());
        payload.put("codeEmploye", employe.getCodeEmploye());
        payload.put("nomComplet", employe.getNomComplet());
        payload.put("cin", employe.getCin());
        payload.put("telephone", employe.getTelephone());
        payload.put("email", employe.getEmail());
        payload.put("poste", employe.getPoste());
        payload.put("actif", employe.isActif());
        return payload;
    }
}




