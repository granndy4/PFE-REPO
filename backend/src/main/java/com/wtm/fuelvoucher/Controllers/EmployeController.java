package com.wtm.fuelvoucher.Controllers;

import com.wtm.fuelvoucher.Services.EmployeService;
import com.wtm.fuelvoucher.Dtos.EmployeActifRequest;
import com.wtm.fuelvoucher.Dtos.EmployeRequest;
import com.wtm.fuelvoucher.Dtos.EmployeResponse;

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
@RequestMapping("/api/employes")
public class EmployeController {

    private final EmployeService employeService;

    public EmployeController(EmployeService employeService) {
        this.employeService = employeService;
    }

    @GetMapping
    public Page<EmployeResponse> list(
            @RequestParam(required = false) Long societeId,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "creeLe", direction = Sort.Direction.DESC) Pageable pageable) {
        return employeService.list(societeId, entrepriseId, actif, search, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeResponse create(@Valid @RequestBody EmployeRequest request,
                                  @AuthenticationPrincipal UserDetails principal) {
        return employeService.create(request, principal.getUsername());
    }

    @PutMapping("/{id}")
    public EmployeResponse update(@PathVariable Long id,
                                  @Valid @RequestBody EmployeRequest request,
                                  @AuthenticationPrincipal UserDetails principal) {
        return employeService.update(id, request, principal.getUsername());
    }

    @PatchMapping("/{id}/actif")
    public EmployeResponse updateActif(@PathVariable Long id,
                                       @Valid @RequestBody EmployeActifRequest request,
                                       @AuthenticationPrincipal UserDetails principal) {
        return employeService.updateActif(id, request.actif(), principal.getUsername());
    }
}




