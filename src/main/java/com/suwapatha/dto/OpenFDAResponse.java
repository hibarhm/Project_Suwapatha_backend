package com.suwapatha.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenFDAResponse {
    private List<Result> results;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonProperty("openfda")
        private OpenFDA openfda;

        @JsonProperty("dosage_and_administration")
        private List<String> dosageAndAdministration;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenFDA {
        @JsonProperty("brand_name")
        private List<String> brandName;

        @JsonProperty("generic_name")
        private List<String> genericName;

        @JsonProperty("manufacturer_name")
        private List<String> manufacturerName;

        @JsonProperty("substance_name")
        private List<String> substanceName;

        @JsonProperty("dosage_form")
        private List<String> dosageForm;
    }
}
