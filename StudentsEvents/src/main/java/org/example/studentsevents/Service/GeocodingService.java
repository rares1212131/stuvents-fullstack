package org.example.studentsevents.Service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeocodingService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    private GeoApiContext context;

    @PostConstruct
    private void init() {
        this.context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public LatLng getCoordinates(String address) {
        try {

            System.out.println("--- GeocodingService: Attempting to geocode address: '" + address + "'");
            System.out.println("--- GeocodingService: Using API Key starting with: " + apiKey.substring(0, 8));
            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();

            if (results != null && results.length > 0) {
                return results[0].geometry.location;
            }
        } catch (Exception e) {
            System.err.println("Error geocoding address: '" + address + "' - " + e.getMessage());
        }
        return null;
    }

    @PreDestroy
    private void cleanup() {
        if (this.context != null) {
            this.context.shutdown();
        }
    }
}