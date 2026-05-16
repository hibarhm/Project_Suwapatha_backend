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
                        "+94 11 269 1111", 6.9197, 79.8679),
                h("Colombo South Teaching Hospital", "Colombo", "Western", "Teaching", "Kalubowila, Dehiwala",
                        "+94 11 271 6111", 6.8771, 79.8856),
                h("Sri Jayewardenepura General Hospital", "Colombo", "Western", "Provincial General",
                        "Kotte, Sri Jayewardenepura", "+94 11 277 7777", 6.8920, 79.9189),
                h("Lady Ridgeway Hospital for Children", "Colombo", "Western", "Specialized", "Borella, Colombo 08",
                        "+94 11 269 3711", 6.9174, 79.8741),
                h("De Soysa Maternity Hospital", "Colombo", "Western", "Maternity", "Kynsey Road, Colombo 08",
                        "+94 11 269 3397", 6.9190, 79.8690),
                h("Castle Street Hospital for Women", "Colombo", "Western", "Specialized", "Castle Street, Colombo 08",
                        "+94 11 269 3742", 6.9130, 79.8780),
                h("Eye Hospital Colombo", "Colombo", "Western", "Specialized", "Dr. N.M. Perera Mawatha, Colombo 10",
                        "+94 11 269 4448", 6.9185, 79.8685),
                h("National Cancer Institute Maharagama", "Colombo", "Western", "Specialized", "Maharagama",
                        "+94 11 285 8029", 6.8510, 79.9240),
                h("Apeksha Hospital", "Colombo", "Western", "Specialized", "Maharagama", "+94 11 285 8036", 6.8510, 79.9240),
                h("National Institute of Psychiatry", "Colombo", "Western", "Specialized", "Angoda", "+94 11 258 0085", 6.9280, 79.9400),
                h("National Hospital for Respiratory Diseases", "Colombo", "Western", "Specialized", "Welisara",
                        "+94 11 295 6397", 7.0250, 79.9150),
                h("Lady Havelock Hospital", "Colombo", "Western", "Maternity", "Lady Havelock Road, Colombo 05",
                        "+94 11 250 0490", 6.8950, 79.8650),
                h("Base Hospital Homagama", "Colombo", "Western", "Base", "Homagama", "+94 11 285 9261", 6.8406, 80.0028),
                h("Base Hospital Negombo", "Gampaha", "Western", "Base", "Negombo", "+94 31 222 2261", 7.2090, 79.8450),
                h("Base Hospital Gampaha", "Gampaha", "Western", "Base", "Gampaha", "+94 33 222 2261", 7.0870, 79.9980),
                h("Base Hospital Wathupitiwala", "Gampaha", "Western", "Base", "Wathupitiwala", "+94 33 227 4261", 7.1250, 80.1250),
                h("Base Hospital Kalutara", "Kalutara", "Western", "Base", "Kalutara", "+94 34 222 2261", 6.5850, 79.9600),
                h("Base Hospital Panadura", "Kalutara", "Western", "Base", "Panadura", "+94 38 223 2261", 6.7150, 79.9050),
                h("Base Hospital Chilaw", "Puttalam", "North Western", "Base", "Chilaw", "+94 32 222 2261", 7.5750, 79.7950),
                h("Base Hospital Avissawella", "Kegalle", "Sabaragamuwa", "Base", "Avissawella", "+94 36 222 2261", 6.9550, 80.2150),

                // ── Central Province ─────────────────────────────────────────
                h("Teaching Hospital Peradeniya", "Kandy", "Central", "Teaching", "Peradeniya", "+94 81 238 8001", 7.2655, 80.5982),
                h("Sirimavo Bandaranaike Children's Hospital", "Kandy", "Central", "Specialized", "Peradeniya",
                        "+94 81 238 8006", 7.2650, 80.5970),
                h("District General Hospital Kandy", "Kandy", "Central", "Provincial General", "Kandy",
                        "+94 81 222 2261", 7.2910, 80.6350),
                h("Base Hospital Nawalapitiya", "Kandy", "Central", "Base", "Nawalapitiya", "+94 54 222 2261", 7.0580, 80.5350),
                h("Base Hospital Matale", "Matale", "Central", "Base", "Matale", "+94 66 222 2261", 7.4720, 80.6230),
                h("Base Hospital Dambulla", "Matale", "Central", "Base", "Dambulla", "+94 66 228 4261", 7.8550, 80.6520),
                h("General Hospital Nuwara Eliya", "Nuwara Eliya", "Central", "Provincial General", "Nuwara Eliya",
                        "+94 52 222 2261", 6.9680, 80.7650),
                h("Base Hospital Hatton", "Nuwara Eliya", "Central", "Base", "Hatton", "+94 51 222 2261", 6.8920, 80.5980),

                // ── Southern Province ─────────────────────────────────────────
                h("Teaching Hospital Karapitiya", "Galle", "Southern", "Teaching", "Karapitiya, Galle",
                        "+94 91 223 4401", 6.0625, 80.2289),
                h("General Hospital Matara", "Matara", "Southern", "Provincial General", "Matara", "+94 41 222 2261", 5.9520, 80.5420),
                h("Base Hospital Tangalle", "Hambantota", "Southern", "Base", "Tangalle", "+94 47 224 0261", 6.0250, 80.7950),
                h("General Hospital Hambantota", "Hambantota", "Southern", "Provincial General", "Hambantota",
                        "+94 47 222 2261", 6.1250, 81.1220),
                h("Base Hospital Embilipitiya", "Ratnapura", "Sabaragamuwa", "Base", "Embilipitiya", "+94 47 226 1261", 6.3380, 80.8480),

                // ── Northern Province ─────────────────────────────────────────
                h("Teaching Hospital Jaffna", "Jaffna", "Northern", "Teaching", "Jaffna", "+94 21 222 2261", 9.6650, 80.0250),
                h("General Hospital Kilinochchi", "Kilinochchi", "Northern", "Provincial General", "Kilinochchi",
                        "+94 21 228 5261", 9.3850, 80.4050),
                h("General Hospital Mannar", "Mannar", "Northern", "Provincial General", "Mannar", "+94 23 222 2261", 8.9820, 79.9150),
                h("General Hospital Mullaitivu", "Mullaitivu", "Northern", "Provincial General", "Mullaitivu",
                        "+94 21 229 0261", 9.2720, 80.8120),
                h("General Hospital Vavuniya", "Vavuniya", "Northern", "Provincial General", "Vavuniya",
                        "+94 24 222 2261", 8.7520, 80.4950),

                // ── Eastern Province ──────────────────────────────────────────
                h("Teaching Hospital Batticaloa", "Batticaloa", "Eastern", "Teaching", "Batticaloa", "+94 65 222 2261", 7.7150, 81.6950),
                h("General Hospital Trincomalee", "Trincomalee", "Eastern", "Provincial General", "Trincomalee",
                        "+94 26 222 2261", 8.5820, 81.2350),
                h("General Hospital Ampara", "Ampara", "Eastern", "Provincial General", "Ampara", "+94 63 222 2261", 7.2850, 81.6750),
                h("Base Hospital Kalmunai", "Ampara", "Eastern", "Base", "Kalmunai", "+94 67 222 2261", 7.4150, 81.8250),
                h("Base Hospital Valaichchenai", "Batticaloa", "Eastern", "Base", "Valaichchenai", "+94 65 225 1261", 7.9150, 81.5350),

                // ── North Western Province ────────────────────────────────────
                h("Teaching Hospital Kurunegala", "Kurunegala", "North Western", "Teaching", "Kurunegala",
                        "+94 37 222 2261", 7.4850, 80.3650),
                h("Base Hospital Kuliyapitiya", "Kurunegala", "North Western", "Base", "Kuliyapitiya",
                        "+94 37 228 1261", 7.4680, 80.0480),
                h("Base Hospital Puttalam", "Puttalam", "North Western", "Base", "Puttalam", "+94 32 226 5261", 8.0350, 79.8280),
                h("Base Hospital Marawila", "Puttalam", "North Western", "Base", "Marawila", "+94 32 225 5261", 7.4150, 79.8350),

                // ── North Central Province ────────────────────────────────────
                h("Teaching Hospital Anuradhapura", "Anuradhapura", "North Central", "Teaching", "Anuradhapura",
                        "+94 25 222 2261", 8.3520, 80.4050),
                h("General Hospital Polonnaruwa", "Polonnaruwa", "North Central", "Provincial General", "Polonnaruwa",
                        "+94 27 222 2261", 7.9350, 81.0020),
                h("Base Hospital Medirigiriya", "Polonnaruwa", "North Central", "Base", "Medirigiriya",
                        "+94 27 224 6261", 8.1350, 80.9950),

                // ── Uva Province ──────────────────────────────────────────────
                h("General Hospital Badulla", "Badulla", "Uva", "Provincial General", "Badulla", "+94 55 222 2261", 6.9850, 81.0550),
                h("General Hospital Monaragala", "Monaragala", "Uva", "Provincial General", "Monaragala",
                        "+94 55 227 6261", 6.8720, 81.3520),
                h("Base Hospital Welimada", "Badulla", "Uva", "Base", "Welimada", "+94 55 225 5261", 6.9020, 80.9150),
                h("Base Hospital Mahiyanganaya", "Badulla", "Uva", "Base", "Mahiyanganaya", "+94 55 225 6261", 7.3250, 81.0050),

                // ── Sabaragamuwa Province ─────────────────────────────────────
                h("General Hospital Ratnapura", "Ratnapura", "Sabaragamuwa", "Provincial General", "Ratnapura",
                        "+94 45 222 2261", 6.6850, 80.4050),
                h("General Hospital Kegalle", "Kegalle", "Sabaragamuwa", "Provincial General", "Kegalle",
                        "+94 35 222 2261", 7.2520, 80.3450),
                h("Base Hospital Balangoda", "Ratnapura", "Sabaragamuwa", "Base", "Balangoda", "+94 45 228 7261", 6.6520, 80.7020),
                h("Base Hospital Dehiowita", "Kegalle", "Sabaragamuwa", "Base", "Dehiowita", "+94 35 226 1261", 6.9850, 80.2650),
                h("General Hospital Mawanella", "Kegalle", "Sabaragamuwa", "Provincial General", "Mawanella", "+94 35 224 6261", 7.2537, 80.4459));
    }

    private Hospital h(String name, String district, String province,
                       String type, String address, String phone, Double lat, Double lng) {
        Hospital hosp = new Hospital();
        hosp.setName(name);
        hosp.setDistrict(district);
        hosp.setProvince(province);
        hosp.setType(type);
        hosp.setAddress(address);
        hosp.setPhone(phone);
        hosp.setLatitude(lat);
        hosp.setLongitude(lng);
        return hosp;
    }
}
