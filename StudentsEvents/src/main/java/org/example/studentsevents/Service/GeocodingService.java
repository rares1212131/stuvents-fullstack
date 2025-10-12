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

    // This injects the API key from your application.properties file
    @Value("${google.maps.api.key}")
    private String apiKey;

    private GeoApiContext context;

    // This method runs after the service is created, setting up the connection to Google
    @PostConstruct
    private void init() {
        this.context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    // This is the main method we will call from our EventService
    public LatLng getCoordinates(String address) {
        try {

            System.out.println("--- GeocodingService: Attempting to geocode address: '" + address + "'");
            System.out.println("--- GeocodingService: Using API Key starting with: " + apiKey.substring(0, 8)); // Print first 8 chars of the key
            // Send the address to the Google Geocoding API
            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();

            // Check if we got any results back
            if (results != null && results.length > 0) {
                // Return the coordinates (LatLng) of the first result
                return results[0].geometry.location;
            }
        } catch (Exception e) {
            // If anything goes wrong (e.g., bad address, API error), log it and return null
            System.err.println("Error geocoding address: '" + address + "' - " + e.getMessage());
        }
        return null; // Return null if no coordinates were found
    }

    // This method runs when the application is shutting down, to clean up resources
    @PreDestroy
    private void cleanup() {
        if (this.context != null) {
            this.context.shutdown();
        }
    }
}