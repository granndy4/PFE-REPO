package com.wtm.fuelvoucher.entreprise;

import com.wtm.fuelvoucher.entreprise.dto.EntrepriseRequest;
import com.wtm.fuelvoucher.entreprise.dto.EntrepriseResponse;
import com.wtm.fuelvoucher.entreprise.dto.EntrepriseStatutRequest;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/entreprises")
public class EntrepriseController {

    private final EntrepriseService entrepriseService;

    public EntrepriseController(EntrepriseService entrepriseService) {
        this.entrepriseService = entrepriseService;
    }

    @GetMapping
    public Page<EntrepriseResponse> list(
            @RequestParam(required = false) Long societeId,
            @RequestParam(required = false) EntrepriseStatut statut,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "creeLe", direction = Sort.Direction.DESC) Pageable pageable) {
        return entrepriseService.list(societeId, statut, search, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntrepriseResponse create(@Valid @RequestBody EntrepriseRequest request,
                                     @AuthenticationPrincipal UserDetails principal) {
        return entrepriseService.create(request, principal.getUsername());
    }

    @PutMapping("/{id}")
    public EntrepriseResponse update(@PathVariable Long id,
                                     @Valid @RequestBody EntrepriseRequest request,
                                     @AuthenticationPrincipal UserDetails principal) {
        return entrepriseService.update(id, request, principal.getUsername());
    }

    @PatchMapping("/{id}/statut")
    public EntrepriseResponse updateStatut(@PathVariable Long id,
                                           @Valid @RequestBody EntrepriseStatutRequest request,
                                           @AuthenticationPrincipal UserDetails principal) {
        return entrepriseService.updateStatut(id, request.statut(), principal.getUsername());
    }
}
