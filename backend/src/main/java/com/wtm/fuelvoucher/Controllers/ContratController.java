package com.wtm.fuelvoucher.Controllers;

import com.wtm.fuelvoucher.Services.ContratService;
import com.wtm.fuelvoucher.Enums.ContratStatut;
import com.wtm.fuelvoucher.Dtos.ContratRequest;
import com.wtm.fuelvoucher.Dtos.ContratResponse;
import com.wtm.fuelvoucher.Dtos.ContratStatutRequest;

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
@RequestMapping("/api/contrats")
public class ContratController {

    private final ContratService contratService;

    public ContratController(ContratService contratService) {
        this.contratService = contratService;
    }

    @GetMapping
    public Page<ContratResponse> list(
            @RequestParam(required = false) Long societeId,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) ContratStatut statut,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "creeLe", direction = Sort.Direction.DESC) Pageable pageable) {
        return contratService.list(societeId, entrepriseId, statut, search, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratResponse create(@Valid @RequestBody ContratRequest request,
                                  @AuthenticationPrincipal UserDetails principal) {
        return contratService.create(request, principal.getUsername());
    }

    @PutMapping("/{id}")
    public ContratResponse update(@PathVariable Long id,
                                  @Valid @RequestBody ContratRequest request,
                                  @AuthenticationPrincipal UserDetails principal) {
        return contratService.update(id, request, principal.getUsername());
    }

    @PatchMapping("/{id}/statut")
    public ContratResponse updateStatut(@PathVariable Long id,
                                        @Valid @RequestBody ContratStatutRequest request,
                                        @AuthenticationPrincipal UserDetails principal) {
        return contratService.updateStatut(id, request.statut(), principal.getUsername());
    }
}



