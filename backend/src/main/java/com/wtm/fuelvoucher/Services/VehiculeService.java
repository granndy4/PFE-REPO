package com.wtm.fuelvoucher.Services;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.wtm.fuelvoucher.Entities.EntrepriseContractee;
import com.wtm.fuelvoucher.Entities.Vehicule;
import com.wtm.fuelvoucher.Repositories.EntrepriseContracteeRepository;
import com.wtm.fuelvoucher.Repositories.VehiculeRepository;
import com.wtm.fuelvoucher.Dtos.VehiculeRequest;
import com.wtm.fuelvoucher.Dtos.VehiculeResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final EntrepriseContracteeRepository entrepriseContracteeRepository;
    private final AuditJournalService auditJournalService;

    public VehiculeService(VehiculeRepository vehiculeRepository,
                           EntrepriseContracteeRepository entrepriseContracteeRepository,
                           AuditJournalService auditJournalService) {
        this.vehiculeRepository = vehiculeRepository;
        this.entrepriseContracteeRepository = entrepriseContracteeRepository;
        this.auditJournalService = auditJournalService;
    }

    @Transactional(readOnly = true)
    public Page<VehiculeResponse> list(Long societeId,
                                       Long entrepriseId,
                                       Boolean actif,
                                       String search,
                                       Pageable pageable) {
        String normalizedSearch = normalizeSearch(search);
        return vehiculeRepository.search(societeId, entrepriseId, actif, normalizedSearch, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public VehiculeResponse create(VehiculeRequest request, String username) {
        String immatriculation = normalizeImmatriculation(request.immatriculation());
        if (vehiculeRepository.existsBySocieteIdAndImmatriculation(request.societeId(), immatriculation)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Immatriculation already exists in this societe");
        }

        EntrepriseContractee entreprise = getEntrepriseOrThrow(request.entrepriseId());
        validateSocieteConsistency(request.societeId(), entreprise);

        Vehicule vehicule = new Vehicule();
        applyRequest(vehicule, request, immatriculation);

        Vehicule saved = vehiculeRepository.save(vehicule);
        auditJournalService.enregistrer(
                "REFERENTIEL_VEHICULE",
                "vehicules",
                saved.getId(),
                "CREATE",
                saved.getSocieteId(),
                username,
                null,
                toAuditPayload(saved));
        return toResponse(saved);
    }

    @Transactional
    public VehiculeResponse update(Long id, VehiculeRequest request, String username) {
        Vehicule vehicule = findByIdOrThrow(id);
        Map<String, Object> anciennesValeurs = toAuditPayload(vehicule);

        String immatriculation = normalizeImmatriculation(request.immatriculation());
        if (vehiculeRepository.existsBySocieteIdAndImmatriculationAndIdNot(request.societeId(), immatriculation, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Immatriculation already exists in this societe");
        }

        EntrepriseContractee entreprise = getEntrepriseOrThrow(request.entrepriseId());
        validateSocieteConsistency(request.societeId(), entreprise);

        applyRequest(vehicule, request, immatriculation);

        Vehicule updated = vehiculeRepository.save(vehicule);
        auditJournalService.enregistrer(
                "REFERENTIEL_VEHICULE",
                "vehicules",
                updated.getId(),
                "UPDATE",
                updated.getSocieteId(),
                username,
                anciennesValeurs,
                toAuditPayload(updated));
        return toResponse(updated);
    }

    @Transactional
    public VehiculeResponse updateActif(Long id, boolean actif, String username) {
        Vehicule vehicule = findByIdOrThrow(id);
        boolean ancienActif = vehicule.isActif();

        vehicule.setActif(actif);
        Vehicule updated = vehiculeRepository.save(vehicule);

        auditJournalService.enregistrer(
                "REFERENTIEL_VEHICULE",
                "vehicules",
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
        Vehicule vehicule = findByIdOrThrow(id);
        Map<String, Object> anciennesValeurs = toAuditPayload(vehicule);

        try {
            vehiculeRepository.delete(vehicule);
            vehiculeRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete vehicule because it is referenced by other records");
        }

        auditJournalService.enregistrer(
                "REFERENTIEL_VEHICULE",
                "vehicules",
                id,
                "DELETE",
                vehicule.getSocieteId(),
                username,
                anciennesValeurs,
                null);
    }

    private Vehicule findByIdOrThrow(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicule not found"));
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

    private void applyRequest(Vehicule vehicule, VehiculeRequest request, String immatriculation) {
        vehicule.setSocieteId(request.societeId());
        vehicule.setEntrepriseId(request.entrepriseId());
        vehicule.setEmployeId(request.employeId());
        vehicule.setImmatriculation(immatriculation);
        vehicule.setCodeFlotte(normalizeNullable(request.codeFlotte()));
        vehicule.setMarque(normalizeNullable(request.marque()));
        vehicule.setModele(normalizeNullable(request.modele()));
        vehicule.setTypeCarburant(request.typeCarburant());
        vehicule.setCapaciteReservoirLitres(request.capaciteReservoirLitres());
        vehicule.setActif(request.actif() == null ? true : request.actif());
    }

    private VehiculeResponse toResponse(Vehicule vehicule) {
        return new VehiculeResponse(
                vehicule.getId(),
                vehicule.getSocieteId(),
                vehicule.getEntrepriseId(),
                vehicule.getEmployeId(),
                vehicule.getImmatriculation(),
                vehicule.getCodeFlotte(),
                vehicule.getMarque(),
                vehicule.getModele(),
                vehicule.getTypeCarburant(),
                vehicule.getCapaciteReservoirLitres(),
                vehicule.isActif(),
                vehicule.getCreeLe(),
                vehicule.getModifieLe());
    }

    private String normalizeImmatriculation(String immatriculation) {
        return immatriculation.trim().toUpperCase(Locale.ROOT);
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

    private Map<String, Object> toAuditPayload(Vehicule vehicule) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", vehicule.getId());
        payload.put("societeId", vehicule.getSocieteId());
        payload.put("entrepriseId", vehicule.getEntrepriseId());
        payload.put("employeId", vehicule.getEmployeId());
        payload.put("immatriculation", vehicule.getImmatriculation());
        payload.put("codeFlotte", vehicule.getCodeFlotte());
        payload.put("marque", vehicule.getMarque());
        payload.put("modele", vehicule.getModele());
        payload.put("typeCarburant", vehicule.getTypeCarburant().name());
        payload.put("capaciteReservoirLitres", vehicule.getCapaciteReservoirLitres());
        payload.put("actif", vehicule.isActif());
        return payload;
    }
}




