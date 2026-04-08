package com.wtm.fuelvoucher.Controllers;

import com.wtm.fuelvoucher.Dtos.BonConsumeRequest;
import com.wtm.fuelvoucher.Dtos.BonCreateRequest;
import com.wtm.fuelvoucher.Dtos.BonRegenerateRequest;
import com.wtm.fuelvoucher.Dtos.BonResponse;
import com.wtm.fuelvoucher.Dtos.BonValidationResponse;
import com.wtm.fuelvoucher.Enums.BonStatut;
import com.wtm.fuelvoucher.Services.BonCarburantService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bons")
public class BonCarburantController {

    private final BonCarburantService bonCarburantService;

    public BonCarburantController(BonCarburantService bonCarburantService) {
        this.bonCarburantService = bonCarburantService;
    }

    @GetMapping
    public Page<BonResponse> list(
            @RequestParam(required = false) Long societeId,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) BonStatut statut,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "creeLe", direction = Sort.Direction.DESC) Pageable pageable) {
        return bonCarburantService.list(societeId, entrepriseId, vehiculeId, statut, search, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BonResponse create(@Valid @RequestBody BonCreateRequest request,
                              @AuthenticationPrincipal UserDetails principal) {
        return bonCarburantService.create(request, principal.getUsername());
    }

    @GetMapping("/validate/{referenceBon}")
    public BonValidationResponse validateByReference(@PathVariable String referenceBon) {
        return bonCarburantService.validateByReference(referenceBon);
    }

    @PostMapping("/{id}/consume")
    public BonResponse consume(@PathVariable Long id,
                               @Valid @RequestBody BonConsumeRequest request,
                               @AuthenticationPrincipal UserDetails principal) {
        return bonCarburantService.consume(id, request, principal.getUsername());
    }

    @PostMapping("/{id}/regenerate")
    public BonResponse regenerate(@PathVariable Long id,
                                  @Valid @RequestBody BonRegenerateRequest request,
                                  @AuthenticationPrincipal UserDetails principal) {
        return bonCarburantService.regenerate(id, request, principal.getUsername());
    }
}
