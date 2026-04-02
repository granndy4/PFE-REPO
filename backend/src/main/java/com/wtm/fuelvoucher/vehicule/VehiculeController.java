package com.wtm.fuelvoucher.vehicule;

import com.wtm.fuelvoucher.vehicule.dto.VehiculeActifRequest;
import com.wtm.fuelvoucher.vehicule.dto.VehiculeRequest;
import com.wtm.fuelvoucher.vehicule.dto.VehiculeResponse;

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
@RequestMapping("/api/vehicules")
public class VehiculeController {

    private final VehiculeService vehiculeService;

    public VehiculeController(VehiculeService vehiculeService) {
        this.vehiculeService = vehiculeService;
    }

    @GetMapping
    public Page<VehiculeResponse> list(
            @RequestParam(required = false) Long societeId,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "creeLe", direction = Sort.Direction.DESC) Pageable pageable) {
        return vehiculeService.list(societeId, entrepriseId, actif, search, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculeResponse create(@Valid @RequestBody VehiculeRequest request,
                                   @AuthenticationPrincipal UserDetails principal) {
        return vehiculeService.create(request, principal.getUsername());
    }

    @PutMapping("/{id}")
    public VehiculeResponse update(@PathVariable Long id,
                                   @Valid @RequestBody VehiculeRequest request,
                                   @AuthenticationPrincipal UserDetails principal) {
        return vehiculeService.update(id, request, principal.getUsername());
    }

    @PatchMapping("/{id}/actif")
    public VehiculeResponse updateActif(@PathVariable Long id,
                                        @Valid @RequestBody VehiculeActifRequest request,
                                        @AuthenticationPrincipal UserDetails principal) {
        return vehiculeService.updateActif(id, request.actif(), principal.getUsername());
    }
}
