package com.wtm.fuelvoucher.Services;

import java.util.Locale;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtm.fuelvoucher.Entities.JournalAudit;
import com.wtm.fuelvoucher.Repositories.JournalAuditRepository;
import com.wtm.fuelvoucher.Repositories.UserAccountRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditJournalService {

    private final JournalAuditRepository journalAuditRepository;
    private final UserAccountRepository userAccountRepository;
    private final ObjectMapper objectMapper;

    public AuditJournalService(JournalAuditRepository journalAuditRepository,
                               UserAccountRepository userAccountRepository,
                               ObjectMapper objectMapper) {
        this.journalAuditRepository = journalAuditRepository;
        this.userAccountRepository = userAccountRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void enregistrer(String typeEvenement,
                            String nomEntite,
                            Long idEntite,
                            String action,
                            Long societeId,
                            String username,
                            Object anciennesValeurs,
                            Object nouvellesValeurs) {
        JournalAudit journalAudit = new JournalAudit();
        journalAudit.setSocieteId(societeId);
        journalAudit.setUtilisateurId(resolveUtilisateurId(username));
        journalAudit.setTypeEvenement(typeEvenement);
        journalAudit.setNomEntite(nomEntite);
        journalAudit.setIdEntite(idEntite);
        journalAudit.setAction(action);
        journalAudit.setAnciennesValeursJson(toJson(anciennesValeurs));
        journalAudit.setNouvellesValeursJson(toJson(nouvellesValeurs));

        HttpServletRequest request = resolveCurrentRequest();
        if (request != null) {
            journalAudit.setAdresseIp(resolveIpAddress(request));
            journalAudit.setAgentUtilisateur(request.getHeader("User-Agent"));
        }

        journalAuditRepository.save(journalAudit);
    }

    private Long resolveUtilisateurId(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        String normalizedEmail = username.trim().toLowerCase(Locale.ROOT);
        return userAccountRepository.findByEmail(normalizedEmail)
                .map(user -> user.getId())
                .orElse(null);
    }

    private HttpServletRequest resolveCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] values = forwardedFor.split(",");
            if (values.length > 0) {
                return values[0].trim();
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private String toJson(Object payload) {
        if (payload == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}




