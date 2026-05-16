package com.suwapatha.service;

import com.suwapatha.dto.MedicineDTO;
import com.suwapatha.dto.DailyMedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MedicineService {

    private final RestTemplate restTemplate;
    private static final String DAILY_MED_API_URL = "https://dailymed.nlm.nih.gov/dailymed/services/v2/spls.json";

    @Cacheable(value = "medicines", key = "#query")
    public List<MedicineDTO> searchMedicines(String query) {
        if (query == null || query.trim().length() < 2) {
            return new ArrayList<>();
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(DAILY_MED_API_URL)
                    .queryParam("drug_name", query)
                    .queryParam("pagesize", 20)
                    .build()
                    .toUriString();

            log.info("Searching DailyMed with URL: {}", url);
            
            DailyMedResponse response = restTemplate.getForObject(url, DailyMedResponse.class);

            if (response == null || response.getData() == null) {
                return new ArrayList<>();
            }

            return response.getData().stream()
                    .map(item -> parseDailyMedTitle(item.getTitle()))
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching data from DailyMed API: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private MedicineDTO parseDailyMedTitle(String title) {
        if (title == null || title.isEmpty()) {
            return new MedicineDTO("N/A", "N/A", "N/A", "N/A");
        }

        String brandName = "N/A";
        String genericName = "N/A";
        String manufacturerName = "N/A";
        String dosageForm = "N/A";

        try {
            // 1. Extract Manufacturer [MANUFACTURER]
            int startBracket = title.indexOf('[');
            int endBracket = title.indexOf(']');
            if (startBracket != -1 && endBracket != -1 && endBracket > startBracket) {
                manufacturerName = title.substring(startBracket + 1, endBracket).trim();
                title = title.substring(0, startBracket).trim(); // Remove manufacturer for further parsing
            }

            // 2. Extract Generic Name (GENERIC)
            int startParen = title.indexOf('(');
            int endParen = title.indexOf(')');
            if (startParen != -1 && endParen != -1 && endParen > startParen) {
                genericName = title.substring(startParen + 1, endParen).trim();
                brandName = title.substring(0, startParen).trim();
                dosageForm = title.substring(endParen + 1).trim();
            } else {
                // No generic name in parens, assume first part is name
                // Try to find common dosage forms to split
                String[] words = title.split("\\s+");
                if (words.length > 1) {
                    brandName = words[0];
                    dosageForm = title.substring(brandName.length()).trim();
                } else {
                    brandName = title;
                }
            }

            if (brandName.isEmpty()) brandName = "N/A";
            if (genericName.isEmpty()) genericName = "N/A";
            if (dosageForm.isEmpty()) dosageForm = "N/A";

        } catch (Exception e) {
            log.warn("Failed to parse DailyMed title: {}", title);
        }

        return MedicineDTO.builder()
                .brandName(brandName)
                .genericName(genericName)
                .manufacturerName(manufacturerName)
                .dosageForm(dosageForm)
                .build();
    }
}
