package com.wtm.fuelvoucher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtm.fuelvoucher.Entities.EntrepriseContractee;
import com.wtm.fuelvoucher.Entities.UserAccount;
import com.wtm.fuelvoucher.Entities.Vehicule;
import com.wtm.fuelvoucher.Enums.EntrepriseStatut;
import com.wtm.fuelvoucher.Enums.Role;
import com.wtm.fuelvoucher.Enums.TypeCarburant;
import com.wtm.fuelvoucher.Repositories.BonCarburantRepository;
import com.wtm.fuelvoucher.Repositories.BonConsommationRepository;
import com.wtm.fuelvoucher.Repositories.ContratRepository;
import com.wtm.fuelvoucher.Repositories.EmployeEntrepriseRepository;
import com.wtm.fuelvoucher.Repositories.EntrepriseContracteeRepository;
import com.wtm.fuelvoucher.Repositories.UserAccountRepository;
import com.wtm.fuelvoucher.Repositories.VehiculeRepository;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
class FuelVoucherApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private EntrepriseContracteeRepository entrepriseContracteeRepository;

    @Autowired
    private VehiculeRepository vehiculeRepository;

    @Autowired
    private BonCarburantRepository bonCarburantRepository;

    @Autowired
    private BonConsommationRepository bonConsommationRepository;

    @Autowired
    private EmployeEntrepriseRepository employeEntrepriseRepository;

    @Autowired
    private ContratRepository contratRepository;

    @BeforeEach
    void setUp() {
        bonConsommationRepository.deleteAll();
        bonCarburantRepository.deleteAll();
        contratRepository.deleteAll();
        vehiculeRepository.deleteAll();
        employeEntrepriseRepository.deleteAll();
        entrepriseContracteeRepository.deleteAll();
        userAccountRepository.deleteAll();

        UserAccount admin = new UserAccount();
        admin.setName("Test Admin");
        admin.setEmail("admin.test@wtm.local");
        admin.setPassword(passwordEncoder.encode("Admin@12345"));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        userAccountRepository.save(admin);
    }

    @Test
    void bonFlow_create_validate_consume_regenerate_and_preventDuplicateTransaction() throws Exception {
        String token = authenticateAndGetToken();

        EntrepriseContractee entreprise = new EntrepriseContractee();
        entreprise.setSocieteId(1L);
        entreprise.setCodeEntreprise("ENT-TEST");
        entreprise.setRaisonSociale("Entreprise Test");
        entreprise.setStatut(EntrepriseStatut.ACTIVE);
        EntrepriseContractee savedEntreprise = entrepriseContracteeRepository.save(entreprise);

        Vehicule vehicule = new Vehicule();
        vehicule.setSocieteId(1L);
        vehicule.setEntrepriseId(savedEntreprise.getId());
        vehicule.setImmatriculation("123-TN-999");
        vehicule.setTypeCarburant(TypeCarburant.DIESEL);
        vehicule.setActif(true);
        Vehicule savedVehicule = vehiculeRepository.save(vehicule);

        Map<String, Object> createPayload = Map.of(
                "societeId", 1,
                "entrepriseId", savedEntreprise.getId(),
                "vehiculeId", savedVehicule.getId(),
                "referenceBon", "BON-IT-001",
                "quantiteInitialeLitres", 100.000,
                "referenceTransactionInitiale", "TX-INIT-001");

        MvcResult createResult = mockMvc.perform(post("/api/bons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.referenceBon").value("BON-IT-001"))
                .andExpect(jsonPath("$.statut").value("ISSUED"))
                .andExpect(jsonPath("$.soldeLitres").value(100.000))
                .andReturn();

        Map<String, Object> createdBon = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class);
        Integer bonId = (Integer) createdBon.get("id");

        mockMvc.perform(get("/api/bons/validate/BON-IT-001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valide").value(true));

        Map<String, Object> consumePayload = Map.of(
                "quantiteLitres", 30.000,
                "referenceTransaction", "TX-CONSUME-001");

        mockMvc.perform(post("/api/bons/{id}/consume", bonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(consumePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("PARTIALLY_CONSUMED"))
                .andExpect(jsonPath("$.soldeLitres").value(70.000));

        mockMvc.perform(post("/api/bons/{id}/consume", bonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(consumePayload)))
                .andExpect(status().isConflict());

        Map<String, Object> regeneratePayload = Map.of("motif", "QR unreadable");
        mockMvc.perform(post("/api/bons/{id}/regenerate", bonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(regeneratePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bonOriginalId").value(bonId))
                .andExpect(jsonPath("$.statut").value("ISSUED"))
                .andExpect(jsonPath("$.soldeLitres").value(70.000));

        mockMvc.perform(get("/api/reports/consumed-bons")
                        .param("societeId", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].referenceBon").value("BON-IT-001"))
                .andExpect(jsonPath("$.content[0].referenceTransaction").value("TX-CONSUME-001"))
                .andExpect(jsonPath("$.content[0].quantiteLitres").value(30.000));

        mockMvc.perform(get("/api/reports/consumed-bons/export.csv")
                        .param("societeId", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("referenceBon")))
                .andExpect(content().string(containsString("BON-IT-001")))
                .andExpect(content().string(containsString("TX-CONSUME-001")));

        mockMvc.perform(get("/api/reports/dashboard/trends")
                        .param("societeId", "1")
                        .param("months", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromPeriod").exists())
                .andExpect(jsonPath("$.toPeriod").exists())
                .andExpect(jsonPath("$.points[0].period").exists())
                .andExpect(jsonPath("$.points[0].bonsIssued").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.points[0].consommationsCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.points[0].quantiteConsommeeLitres").value(greaterThanOrEqualTo(30.0)))
                .andExpect(jsonPath("$.points[0].auditsCount").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void deleteEntreprise_returnsNoContent() throws Exception {
        String token = authenticateAndGetToken();

        EntrepriseContractee entreprise = new EntrepriseContractee();
        entreprise.setSocieteId(2L);
        entreprise.setCodeEntreprise("ENT-DEL");
        entreprise.setRaisonSociale("Delete Me");
        entreprise.setStatut(EntrepriseStatut.ACTIVE);
        EntrepriseContractee saved = entrepriseContracteeRepository.save(entreprise);

        mockMvc.perform(delete("/api/entreprises/{id}", saved.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

        @Test
        void auditHistory_returnsEntrepriseAuditEvent() throws Exception {
                String token = authenticateAndGetToken();

                Map<String, Object> createPayload = Map.of(
                        "societeId", 3,
                        "codeEntreprise", "ENT-AUD",
                        "raisonSociale", "Audit Target",
                        "statut", "ACTIVE");

                MvcResult createResult = mockMvc.perform(post("/api/entreprises")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .content(objectMapper.writeValueAsString(createPayload)))
                        .andExpect(status().isCreated())
                        .andReturn();

                Map<String, Object> createdEntreprise = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class);
                Integer entrepriseId = (Integer) createdEntreprise.get("id");

                mockMvc.perform(get("/api/reports/audit-history")
                                                .param("societeId", "3")
                                                .param("nomEntite", "entreprises_contractees")
                                .param("idEntite", String.valueOf(entrepriseId))
                                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].nomEntite").value("entreprises_contractees"))
                        .andExpect(jsonPath("$.content[0].idEntite").value(entrepriseId))
                                .andExpect(jsonPath("$.content[0].action").value("CREATE"));

                mockMvc.perform(get("/api/reports/audit-history/export.csv")
                                                .param("societeId", "3")
                                                .param("nomEntite", "entreprises_contractees")
                                                .param("idEntite", String.valueOf(entrepriseId))
                                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("nomEntite")))
                                .andExpect(content().string(containsString("entreprises_contractees")))
                                .andExpect(content().string(containsString("CREATE")));
        }

        @Test
        void dashboardSummary_returnsOperationalCounts() throws Exception {
                String token = authenticateAndGetToken();

                mockMvc.perform(get("/api/reports/dashboard")
                                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.entreprisesTotal").value(0))
                                .andExpect(jsonPath("$.bonsTotal").value(0))
                        .andExpect(jsonPath("$.auditsTotal").value(greaterThanOrEqualTo(1)));
        }

    private String authenticateAndGetToken() throws Exception {
        Map<String, Object> loginPayload = Map.of(
                "email", "admin.test@wtm.local",
                "password", "Admin@12345");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> authResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        return (String) authResponse.get("token");
    }
}
