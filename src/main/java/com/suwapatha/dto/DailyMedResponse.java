package com.suwapatha.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyMedResponse {
    private List<SPLInfo> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SPLInfo {
        private String title;
        private String setid;
    }
}
