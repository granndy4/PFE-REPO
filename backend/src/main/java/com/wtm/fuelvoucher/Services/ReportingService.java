package com.wtm.fuelvoucher.Services;

import com.wtm.fuelvoucher.Dtos.DashboardSummaryResponse;
import com.wtm.fuelvoucher.Dtos.DashboardTrendPointResponse;
import com.wtm.fuelvoucher.Dtos.DashboardTrendsResponse;
import com.wtm.fuelvoucher.Dtos.BonConsommationReportResponse;
import com.wtm.fuelvoucher.Dtos.JournalAuditReportResponse;
import com.wtm.fuelvoucher.Entities.BonCarburant;
import com.wtm.fuelvoucher.Entities.BonConsommation;
import com.wtm.fuelvoucher.Entities.JournalAudit;
import com.wtm.fuelvoucher.Enums.BonStatut;
import com.wtm.fuelvoucher.Enums.ContratStatut;
import com.wtm.fuelvoucher.Enums.EntrepriseStatut;
import com.wtm.fuelvoucher.Repositories.BonConsommationRepository;
import com.wtm.fuelvoucher.Repositories.BonCarburantRepository;
import com.wtm.fuelvoucher.Repositories.ContratRepository;
import com.wtm.fuelvoucher.Repositories.EmployeEntrepriseRepository;
import com.wtm.fuelvoucher.Repositories.EntrepriseContracteeRepository;
import com.wtm.fuelvoucher.Repositories.JournalAuditRepository;
import com.wtm.fuelvoucher.Repositories.VehiculeRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportingService {

    private final EntrepriseContracteeRepository entrepriseContracteeRepository;
    private final ContratRepository contratRepository;
    private final VehiculeRepository vehiculeRepository;
    private final EmployeEntrepriseRepository employeEntrepriseRepository;
    private final BonCarburantRepository bonCarburantRepository;
    private final BonConsommationRepository bonConsommationRepository;
    private final JournalAuditRepository journalAuditRepository;

    public ReportingService(EntrepriseContracteeRepository entrepriseContracteeRepository,
                            ContratRepository contratRepository,
                            VehiculeRepository vehiculeRepository,
                            EmployeEntrepriseRepository employeEntrepriseRepository,
                            BonCarburantRepository bonCarburantRepository,
                            BonConsommationRepository bonConsommationRepository,
                            JournalAuditRepository journalAuditRepository) {
        this.entrepriseContracteeRepository = entrepriseContracteeRepository;
        this.contratRepository = contratRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.employeEntrepriseRepository = employeEntrepriseRepository;
        this.bonCarburantRepository = bonCarburantRepository;
        this.bonConsommationRepository = bonConsommationRepository;
        this.journalAuditRepository = journalAuditRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse dashboardSummary() {
        long entreprisesTotal = entrepriseContracteeRepository.count();
        long entreprisesActive = entrepriseContracteeRepository.countByStatut(EntrepriseStatut.ACTIVE);
        long entreprisesSuspended = entrepriseContracteeRepository.countByStatut(EntrepriseStatut.SUSPENDED);
        long contratsTotal = contratRepository.count();
        long contratsActive = contratRepository.countByStatut(ContratStatut.ACTIVE);
        long vehiculesTotal = vehiculeRepository.count();
        long vehiculesActifs = vehiculeRepository.countByActif(true);
        long employesTotal = employeEntrepriseRepository.count();
        long employesActifs = employeEntrepriseRepository.countByActif(true);
        long bonsTotal = bonCarburantRepository.count();
        long bonsIssued = bonCarburantRepository.countByStatut(BonStatut.ISSUED);
        long bonsPartiallyConsumed = bonCarburantRepository.countByStatut(BonStatut.PARTIALLY_CONSUMED);
        long bonsConsumed = bonCarburantRepository.countByStatut(BonStatut.CONSUMED);
        long bonsRegenerated = bonCarburantRepository.countByStatut(BonStatut.REGENERATED);
        long consommationsTotal = bonConsommationRepository.count();
        long auditsTotal = journalAuditRepository.count();

        return new DashboardSummaryResponse(
                entreprisesTotal,
                entreprisesActive,
                entreprisesSuspended,
                contratsTotal,
                contratsActive,
                vehiculesTotal,
                vehiculesActifs,
                employesTotal,
                employesActifs,
                bonsTotal,
                bonsIssued,
                bonsPartiallyConsumed,
                bonsConsumed,
                bonsRegenerated,
                consommationsTotal,
                auditsTotal);
    }

    @Transactional(readOnly = true)
    public DashboardTrendsResponse dashboardTrends(Long societeId, Integer months) {
        int normalizedMonths = months == null ? 12 : months;
        if (normalizedMonths < 1 || normalizedMonths > 36) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "months must be between 1 and 36");
        }

        YearMonth toPeriod = YearMonth.now(ZoneOffset.UTC);
        YearMonth fromPeriod = toPeriod.minusMonths(normalizedMonths - 1L);

        Instant fromInclusive = fromPeriod.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toExclusive = toPeriod.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<BonCarburant> issuedBons = bonCarburantRepository.listCreatedForDashboardTrends(
                societeId,
                fromInclusive,
                toExclusive);
        List<BonConsommation> consumptions = bonConsommationRepository.listForDashboardTrends(
                societeId,
                fromInclusive,
                toExclusive);
        List<JournalAudit> audits = journalAuditRepository.listForDashboardTrends(
                societeId,
                fromInclusive,
                toExclusive);

        Map<YearMonth, TrendAccumulator> grouped = new HashMap<>();

        for (BonCarburant bon : issuedBons) {
            YearMonth period = toYearMonth(bon.getCreeLe());
            if (period != null) {
                grouped.computeIfAbsent(period, key -> new TrendAccumulator()).bonsIssued++;
            }
        }

        for (BonConsommation consumption : consumptions) {
            YearMonth period = toYearMonth(consumption.getConsommeLe());
            if (period != null) {
                TrendAccumulator accumulator = grouped.computeIfAbsent(period, key -> new TrendAccumulator());
                accumulator.consommationsCount++;
                accumulator.quantiteConsommeeLitres = accumulator.quantiteConsommeeLitres
                        .add(consumption.getQuantiteLitres() == null ? BigDecimal.ZERO : consumption.getQuantiteLitres());
            }
        }

        for (JournalAudit audit : audits) {
            YearMonth period = toYearMonth(audit.getCreeLe());
            if (period != null) {
                grouped.computeIfAbsent(period, key -> new TrendAccumulator()).auditsCount++;
            }
        }

        List<DashboardTrendPointResponse> points = new ArrayList<>(normalizedMonths);
        for (int i = 0; i < normalizedMonths; i++) {
            YearMonth period = fromPeriod.plusMonths(i);
            TrendAccumulator accumulator = grouped.getOrDefault(period, new TrendAccumulator());
            points.add(new DashboardTrendPointResponse(
                    period.toString(),
                    accumulator.bonsIssued,
                    accumulator.consommationsCount,
                    accumulator.quantiteConsommeeLitres,
                    accumulator.auditsCount));
        }

        return new DashboardTrendsResponse(
                societeId,
                fromPeriod.toString(),
                toPeriod.toString(),
                points);
    }

    @Transactional(readOnly = true)
    public Page<BonConsommationReportResponse> consumedBons(Long societeId,
                                                            Long entrepriseId,
                                                            LocalDate fromDate,
                                                            LocalDate toDate,
                                                            Pageable pageable) {
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toDate must be on or after fromDate");
        }

        Instant fromInclusive = fromDate == null ? null : fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toExclusive = toDate == null ? null : toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        return bonConsommationRepository.searchConsumptionReport(societeId, entrepriseId, fromInclusive, toExclusive, pageable);
    }

    @Transactional(readOnly = true)
    public String exportConsumedBonsCsv(Long societeId,
                                        Long entrepriseId,
                                        LocalDate fromDate,
                                        LocalDate toDate) {
        DateRange dateRange = resolveDateRange(fromDate, toDate);
        List<BonConsommationReportResponse> rows = bonConsommationRepository.listConsumptionReport(
                societeId,
                entrepriseId,
                dateRange.fromInclusive(),
                dateRange.toExclusive());

        StringBuilder csv = new StringBuilder();
        csv.append("consommationId,bonId,referenceBon,societeId,entrepriseId,vehiculeId,referenceTransaction,quantiteLitres,consommeLe,consommeParUtilisateurId,notes\n");
        for (BonConsommationReportResponse row : rows) {
            csv.append(row.consommationId()).append(',')
                    .append(row.bonId()).append(',')
                    .append(escapeCsv(row.referenceBon())).append(',')
                    .append(row.societeId()).append(',')
                    .append(row.entrepriseId()).append(',')
                    .append(row.vehiculeId()).append(',')
                    .append(escapeCsv(row.referenceTransaction())).append(',')
                    .append(row.quantiteLitres()).append(',')
                    .append(row.consommeLe()).append(',')
                    .append(row.consommeParUtilisateurId() == null ? "" : row.consommeParUtilisateurId()).append(',')
                    .append(escapeCsv(row.notes()))
                    .append('\n');
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public Page<JournalAuditReportResponse> auditHistory(Long societeId,
                                                         String nomEntite,
                                                         Long idEntite,
                                                         LocalDate fromDate,
                                                         LocalDate toDate,
                                                         Pageable pageable) {
        DateRange dateRange = resolveDateRange(fromDate, toDate);

        String normalizedEntity = nomEntite == null || nomEntite.isBlank() ? null : nomEntite.trim();

        return journalAuditRepository.searchAuditHistory(
                societeId,
                normalizedEntity,
                idEntite,
                dateRange.fromInclusive(),
                dateRange.toExclusive(),
                pageable);
    }

    @Transactional(readOnly = true)
    public String exportAuditHistoryCsv(Long societeId,
                                        String nomEntite,
                                        Long idEntite,
                                        LocalDate fromDate,
                                        LocalDate toDate) {
        DateRange dateRange = resolveDateRange(fromDate, toDate);
        String normalizedEntity = nomEntite == null || nomEntite.isBlank() ? null : nomEntite.trim();

        List<JournalAuditReportResponse> rows = journalAuditRepository.listAuditHistory(
                societeId,
                normalizedEntity,
                idEntite,
                dateRange.fromInclusive(),
                dateRange.toExclusive());

        StringBuilder csv = new StringBuilder();
        csv.append("id,societeId,utilisateurId,typeEvenement,nomEntite,idEntite,action,adresseIp,agentUtilisateur,anciennesValeursJson,nouvellesValeursJson,creeLe\n");
        for (JournalAuditReportResponse row : rows) {
            csv.append(row.id()).append(',')
                    .append(row.societeId() == null ? "" : row.societeId()).append(',')
                    .append(row.utilisateurId() == null ? "" : row.utilisateurId()).append(',')
                    .append(escapeCsv(row.typeEvenement())).append(',')
                    .append(escapeCsv(row.nomEntite())).append(',')
                    .append(row.idEntite() == null ? "" : row.idEntite()).append(',')
                    .append(escapeCsv(row.action())).append(',')
                    .append(escapeCsv(row.adresseIp())).append(',')
                    .append(escapeCsv(row.agentUtilisateur())).append(',')
                    .append(escapeCsv(row.anciennesValeursJson())).append(',')
                    .append(escapeCsv(row.nouvellesValeursJson())).append(',')
                    .append(row.creeLe())
                    .append('\n');
        }
        return csv.toString();
    }

    private DateRange resolveDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toDate must be on or after fromDate");
        }

        Instant fromInclusive = fromDate == null ? null : fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toExclusive = toDate == null ? null : toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return new DateRange(fromInclusive, toExclusive);
    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private YearMonth toYearMonth(Instant instant) {
        if (instant == null) {
            return null;
        }
        return YearMonth.from(instant.atZone(ZoneOffset.UTC));
    }

    private static final class TrendAccumulator {
        private long bonsIssued;
        private long consommationsCount;
        private BigDecimal quantiteConsommeeLitres = BigDecimal.ZERO;
        private long auditsCount;
    }

    private record DateRange(Instant fromInclusive, Instant toExclusive) {
    }
}