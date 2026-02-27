package com.suwapatha.service;

import com.suwapatha.entity.Hospital;
import com.suwapatha.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HospitalDataSeeder implements ApplicationRunner {

    private final HospitalRepository hospitalRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (hospitalRepository.count() > 0) {
            log.info("Hospitals already seeded — skipping.");
            return;
        }
        log.info("Seeding Sri Lankan government hospitals...");
        hospitalRepository.saveAll(hospitals());
        log.info("Seeded {} hospitals.", hospitalRepository.count());
    }

    private List<Hospital> hospitals() {
        return List.of(
                // ── Western Province ──────────────────────────────────────────
                h("National Hospital of Sri Lanka", "Colombo", "Western", "Teaching", "Regent Street, Colombo 10",
                        "+94 11 269 1111"),
                h("Colombo South Teaching Hospital", "Colombo", "Western", "Teaching", "Kalubowila, Dehiwala",
                        "+94 11 271 6111"),
                h("Sri Jayewardenepura General Hospital", "Colombo", "Western", "Provincial General",
                        "Kotte, Sri Jayewardenepura", "+94 11 277 7777"),
                h("Lady Ridgeway Hospital for Children", "Colombo", "Western", "Specialized", "Borella, Colombo 08",
                        "+94 11 269 3711"),
                h("De Soysa Maternity Hospital", "Colombo", "Western", "Maternity", "Kynsey Road, Colombo 08",
                        "+94 11 269 3397"),
                h("Castle Street Hospital for Women", "Colombo", "Western", "Specialized", "Castle Street, Colombo 08",
                        "+94 11 269 3742"),
                h("Eye Hospital Colombo", "Colombo", "Western", "Specialized", "Dr. N.M. Perera Mawatha, Colombo 10",
                        "+94 11 269 4448"),
                h("National Cancer Institute Maharagama", "Colombo", "Western", "Specialized", "Maharagama",
                        "+94 11 285 8029"),
                h("Apeksha Hospital", "Colombo", "Western", "Specialized", "Maharagama", "+94 11 285 8036"),
                h("National Institute of Psychiatry", "Colombo", "Western", "Specialized", "Angoda", "+94 11 258 0085"),
                h("National Hospital for Respiratory Diseases", "Colombo", "Western", "Specialized", "Welisara",
                        "+94 11 295 6397"),
                h("Lady Havelock Hospital", "Colombo", "Western", "Maternity", "Lady Havelock Road, Colombo 05",
                        "+94 11 250 0490"),
                h("Base Hospital Homagama", "Colombo", "Western", "Base", "Homagama", "+94 11 285 9261"),
                h("Base Hospital Negombo", "Gampaha", "Western", "Base", "Negombo", "+94 31 222 2261"),
                h("Base Hospital Gampaha", "Gampaha", "Western", "Base", "Gampaha", "+94 33 222 2261"),
                h("Base Hospital Wathupitiwala", "Gampaha", "Western", "Base", "Wathupitiwala", "+94 33 227 4261"),
                h("Base Hospital Kalutara", "Kalutara", "Western", "Base", "Kalutara", "+94 34 222 2261"),
                h("Base Hospital Panadura", "Kalutara", "Western", "Base", "Panadura", "+94 38 223 2261"),
                h("Base Hospital Chilaw", "Puttalam", "North Western", "Base", "Chilaw", "+94 32 222 2261"),
                h("Base Hospital Avissawella", "Kegalle", "Sabaragamuwa", "Base", "Avissawella", "+94 36 222 2261"),

                // ── Central Province ─────────────────────────────────────────
                h("Teaching Hospital Peradeniya", "Kandy", "Central", "Teaching", "Peradeniya", "+94 81 238 8001"),
                h("Sirimavo Bandaranaike Children's Hospital", "Kandy", "Central", "Specialized", "Peradeniya",
                        "+94 81 238 8006"),
                h("District General Hospital Kandy", "Kandy", "Central", "Provincial General", "Kandy",
                        "+94 81 222 2261"),
                h("Base Hospital Nawalapitiya", "Kandy", "Central", "Base", "Nawalapitiya", "+94 54 222 2261"),
                h("Base Hospital Matale", "Matale", "Central", "Base", "Matale", "+94 66 222 2261"),
                h("Base Hospital Dambulla", "Matale", "Central", "Base", "Dambulla", "+94 66 228 4261"),
                h("General Hospital Nuwara Eliya", "Nuwara Eliya", "Central", "Provincial General", "Nuwara Eliya",
                        "+94 52 222 2261"),
                h("Base Hospital Hatton", "Nuwara Eliya", "Central", "Base", "Hatton", "+94 51 222 2261"),

                // ── Southern Province ─────────────────────────────────────────
                h("Teaching Hospital Karapitiya", "Galle", "Southern", "Teaching", "Karapitiya, Galle",
                        "+94 91 223 4401"),
                h("General Hospital Matara", "Matara", "Southern", "Provincial General", "Matara", "+94 41 222 2261"),
                h("Base Hospital Tangalle", "Hambantota", "Southern", "Base", "Tangalle", "+94 47 224 0261"),
                h("General Hospital Hambantota", "Hambantota", "Southern", "Provincial General", "Hambantota",
                        "+94 47 222 2261"),
                h("Base Hospital Embilipitiya", "Ratnapura", "Sabaragamuwa", "Base", "Embilipitiya", "+94 47 226 1261"),

                // ── Northern Province ─────────────────────────────────────────
                h("Teaching Hospital Jaffna", "Jaffna", "Northern", "Teaching", "Jaffna", "+94 21 222 2261"),
                h("General Hospital Kilinochchi", "Kilinochchi", "Northern", "Provincial General", "Kilinochchi",
                        "+94 21 228 5261"),
                h("General Hospital Mannar", "Mannar", "Northern", "Provincial General", "Mannar", "+94 23 222 2261"),
                h("General Hospital Mullaitivu", "Mullaitivu", "Northern", "Provincial General", "Mullaitivu",
                        "+94 21 229 0261"),
                h("General Hospital Vavuniya", "Vavuniya", "Northern", "Provincial General", "Vavuniya",
                        "+94 24 222 2261"),

                // ── Eastern Province ──────────────────────────────────────────
                h("Teaching Hospital Batticaloa", "Batticaloa", "Eastern", "Teaching", "Batticaloa", "+94 65 222 2261"),
                h("General Hospital Trincomalee", "Trincomalee", "Eastern", "Provincial General", "Trincomalee",
                        "+94 26 222 2261"),
                h("General Hospital Ampara", "Ampara", "Eastern", "Provincial General", "Ampara", "+94 63 222 2261"),
                h("Base Hospital Kalmunai", "Ampara", "Eastern", "Base", "Kalmunai", "+94 67 222 2261"),
                h("Base Hospital Valaichchenai", "Batticaloa", "Eastern", "Base", "Valaichchenai", "+94 65 225 1261"),

                // ── North Western Province ────────────────────────────────────
                h("Teaching Hospital Kurunegala", "Kurunegala", "North Western", "Teaching", "Kurunegala",
                        "+94 37 222 2261"),
                h("Base Hospital Kuliyapitiya", "Kurunegala", "North Western", "Base", "Kuliyapitiya",
                        "+94 37 228 1261"),
                h("Base Hospital Puttalam", "Puttalam", "North Western", "Base", "Puttalam", "+94 32 226 5261"),
                h("Base Hospital Marawila", "Puttalam", "North Western", "Base", "Marawila", "+94 32 225 5261"),

                // ── North Central Province ────────────────────────────────────
                h("Teaching Hospital Anuradhapura", "Anuradhapura", "North Central", "Teaching", "Anuradhapura",
                        "+94 25 222 2261"),
                h("General Hospital Polonnaruwa", "Polonnaruwa", "North Central", "Provincial General", "Polonnaruwa",
                        "+94 27 222 2261"),
                h("Base Hospital Medirigiriya", "Polonnaruwa", "North Central", "Base", "Medirigiriya",
                        "+94 27 224 6261"),

                // ── Uva Province ──────────────────────────────────────────────
                h("General Hospital Badulla", "Badulla", "Uva", "Provincial General", "Badulla", "+94 55 222 2261"),
                h("General Hospital Monaragala", "Monaragala", "Uva", "Provincial General", "Monaragala",
                        "+94 55 227 6261"),
                h("Base Hospital Welimada", "Badulla", "Uva", "Base", "Welimada", "+94 55 225 5261"),
                h("Base Hospital Mahiyanganaya", "Badulla", "Uva", "Base", "Mahiyanganaya", "+94 55 225 6261"),

                // ── Sabaragamuwa Province ─────────────────────────────────────
                h("General Hospital Ratnapura", "Ratnapura", "Sabaragamuwa", "Provincial General", "Ratnapura",
                        "+94 45 222 2261"),
                h("General Hospital Kegalle", "Kegalle", "Sabaragamuwa", "Provincial General", "Kegalle",
                        "+94 35 222 2261"),
                h("Base Hospital Balangoda", "Ratnapura", "Sabaragamuwa", "Base", "Balangoda", "+94 45 228 7261"),
                h("Base Hospital Dehiowita", "Kegalle", "Sabaragamuwa", "Base", "Dehiowita", "+94 35 226 1261"));
    }

    private Hospital h(String name, String district, String province,
                       String type, String address, String phone) {
        Hospital hosp = new Hospital();
        hosp.setName(name);
        hosp.setDistrict(district);
        hosp.setProvince(province);
        hosp.setType(type);
        hosp.setAddress(address);
        hosp.setPhone(phone);
        return hosp;
    }
}
