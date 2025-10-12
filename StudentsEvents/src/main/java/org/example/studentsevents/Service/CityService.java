package org.example.studentsevents.Service;

import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTORequest.CityRequest;
import org.example.studentsevents.DTOResponse.CityResponse;
import org.example.studentsevents.Repository.EventRepository;
import org.example.studentsevents.model.City;
import org.example.studentsevents.Repository.CityRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final ModelMapper modelMapper;
    private final EventRepository eventRepository;

    @Transactional
    public CityResponse createCity(CityRequest cityRequest) {
        if (cityRepository.existsByNameIgnoreCase(cityRequest.getName())) {
            throw new IllegalStateException("City with name '" + cityRequest.getName() + "' already exists.");
        }
        City newCity = modelMapper.map(cityRequest, City.class);
        City savedCity = cityRepository.save(newCity);
        return modelMapper.map(savedCity, CityResponse.class);
    }

    @Transactional(readOnly = true)
    public CityResponse getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + id));
        return modelMapper.map(city, CityResponse.class);
    }

    @Transactional(readOnly = true)
    public List<CityResponse> getAllCities() {
        List<City> cities = cityRepository.findAll();
        return cities.stream()
                .map(city -> modelMapper.map(city, CityResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public CityResponse updateCity(Long id, CityRequest cityRequest) {
        City existingCity = cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + id));
        existingCity.setName(cityRequest.getName());
        City savedCity = cityRepository.save(existingCity);
        return modelMapper.map(savedCity, CityResponse.class);
    }

    @Transactional
    public void deleteCity(Long id) {
        if (eventRepository.existsByCityId(id)) {
            throw new IllegalStateException("Cannot delete this city because it is currently in use by one or more events.");
        }

        if (!cityRepository.existsById(id)) {
            throw new RuntimeException("City not found with id: " + id);
        }

        // This line only runs if the check passes.
        cityRepository.deleteById(id);
    }
}