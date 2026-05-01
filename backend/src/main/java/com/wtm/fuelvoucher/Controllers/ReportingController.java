package com.wtm.fuelvoucher.Controllers;

import com.wtm.fuelvoucher.Dtos.DashboardSummaryResponse;
import com.wtm.fuelvoucher.Dtos.DashboardTrendsResponse;
import com.wtm.fuelvoucher.Dtos.BonConsommationReportResponse;
import com.wtm.fuelvoucher.Dtos.JournalAuditReportResponse;
import com.wtm.fuelvoucher.Services.ReportingService;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/dashboard")
    public DashboardSummaryResponse dashboardSummary() {
        return reportingService.dashboardSummary();
    }

    @GetMapping("/dashboard/trends")
    public DashboardTrendsResponse dashboardTrends(
            @RequestParam(required = false) Long societeId,
            @RequestParam(required = false) Integer months) {
        return reportingService.dashboardTrends(societeId, months);
    }

    @GetMapping("/consumed-bons")
    public Page<BonConsommationReportResponse> consumedBons(
            @RequestParam(required = false) Long societeId,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @PageableDefault(size = 20, sort = "consommeLe", direction = Sort.Direction.DESC) Pageable pageable) {
        return reportingService.consumedBons(societeId, entrepriseId, fromDate, toDate, pageable);
    }

    @GetMapping(value = "/consumed-bons/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportConsumedBonsCsv(
            @RequestParam(required = false) Long societeId,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        String csv = reportingService.exportConsumedBonsCsv(societeId, entrepriseId, fromDate, toDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=consumed-bons-report.csv")
                .contentType(new MediaType("text", "csv"))
                .body(csv);
    }

    @GetMapping("/audit-history")
    public Page<JournalAuditReportResponse> auditHistory(
            @RequestParam(required = false) Long societeId,
            @RequestParam(required = false) String nomEntite,
            @RequestParam(required = false) Long idEntite,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @PageableDefault(size = 20, sort = "creeLe", direction = Sort.Direction.DESC) Pageable pageable) {
        return reportingService.auditHistory(societeId, nomEntite, idEntite, fromDate, toDate, pageable);
    }

    @GetMapping(value = "/audit-history/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportAuditHistoryCsv(
            @RequestParam(required = false) Long societeId,
            @RequestParam(required = false) String nomEntite,
            @RequestParam(required = false) Long idEntite,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        String csv = reportingService.exportAuditHistoryCsv(societeId, nomEntite, idEntite, fromDate, toDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-history-report.csv")
                .contentType(new MediaType("text", "csv"))
                .body(csv);
    }
}