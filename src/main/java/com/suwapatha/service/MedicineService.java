package com.suwapatha.service;

import com.suwapatha.dto.MedicineDTO;
import com.suwapatha.dto.OpenFDAResponse;
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
    private static final String FDA_API_URL = "https://api.fda.gov/drug/label.json";

    @Cacheable(value = "medicines", key = "#query")
    public List<MedicineDTO> searchMedicines(String query) {
        if (query == null || query.trim().length() < 2) {
            return new ArrayList<>();
        }

        try {
            // Search in brand_name, generic_name, and substance_name
            String searchQuery = String.format("openfda.brand_name:\"%s*\" openfda.generic_name:\"%s*\" openfda.substance_name:\"%s*\"", 
                    query, query, query);

            String url = UriComponentsBuilder.fromHttpUrl(FDA_API_URL)
                    .queryParam("search", searchQuery)
                    .queryParam("limit", 10)
                    .build()
                    .toUriString();

            log.info("Searching openFDA with URL: {}", url);
            
            OpenFDAResponse response = restTemplate.getForObject(url, OpenFDAResponse.class);

            if (response == null || response.getResults() == null) {
                return new ArrayList<>();
            }

            return response.getResults().stream()
                    .filter(result -> result.getOpenfda() != null)
                    .map(result -> {
                        OpenFDAResponse.OpenFDA fda = result.getOpenfda();
                        return MedicineDTO.builder()
                                .brandName(getFirstOrEmpty(fda.getBrandName()))
                                .genericName(getFirstOrEmpty(fda.getGenericName()))
                                .manufacturerName(getFirstOrEmpty(fda.getManufacturerName()))
                                .dosageForm(getFirstOrEmpty(fda.getDosageForm()))
                                .build();
                    })
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching data from openFDA API: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String getFirstOrEmpty(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "N/A";
        }
        return list.get(0);
    }
}
