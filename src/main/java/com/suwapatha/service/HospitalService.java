package com.suwapatha.service;

import com.suwapatha.dto.NearbyHospitalResponse;
import com.suwapatha.entity.Hospital;
import com.suwapatha.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    public List<NearbyHospitalResponse> getNearbyHospitals(double lat, double lng) {
        List<Hospital> hospitals = hospitalRepository.findAll();

        return hospitals.stream()
                .filter(h -> h.getLatitude() != null && h.getLongitude() != null)
                .map(h -> {
                    double distance = calculateDistance(lat, lng, h.getLatitude(), h.getLongitude());
                    return new NearbyHospitalResponse(
                            h.getId(),
                            h.getName(),
                            h.getAddress(),
                            h.getType(),
                            h.getPhone(),
                            h.getLatitude(),
                            h.getLongitude(),
                            distance
                    );
                })
                .sorted(Comparator.comparingDouble(NearbyHospitalResponse::getDistanceKm))
                .limit(10) // Top 10 nearby hospitals
                .collect(Collectors.toList());
    }

    /* Haversine formula to calculate distance between two points in km */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
