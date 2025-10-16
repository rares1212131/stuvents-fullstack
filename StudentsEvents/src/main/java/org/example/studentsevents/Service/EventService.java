// FILE: D:\students-fullstack\StudentsEvents\src\main\java\org\example\studentsevents\Service\EventService.java

package org.example.studentsevents.Service;

import com.google.maps.model.LatLng;
import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTORequest.EventRequest;
import org.example.studentsevents.DTORequest.TicketTypeRequest;
import org.example.studentsevents.DTOResponse.*;
import org.example.studentsevents.Repository.*;
import org.example.studentsevents.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ImageService imageService; // The refactored Cloudinary service
    private final GeocodingService geocodingService;

    // =====================================================================================
    // == 1. PUBLIC-FACING METHODS (For any site visitor)
    // =====================================================================================

    @Transactional(readOnly = true)
    public EventResponse getPublicEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return mapToPublicEventResponse(event);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> searchPublicEvents(String eventName, String categoryName, String cityName, Pageable pageable) {
        Page<Event> eventPage;
        if (categoryName != null && cityName != null) {
            eventPage = eventRepository.findByCategoryNameIgnoreCaseAndCityNameIgnoreCase(categoryName, cityName, pageable);
        } else if (categoryName != null) {
            eventPage = eventRepository.findByCategoryNameIgnoreCase(categoryName, pageable);
        } else if (cityName != null) {
            eventPage = eventRepository.findByCityNameIgnoreCase(cityName, pageable);
        } else if (eventName != null) {
            eventPage = eventRepository.findByNameContainingIgnoreCase(eventName, pageable);
        } else {
            eventPage = eventRepository.findAll(pageable);
        }
        return eventPage.map(this::mapToPublicEventResponse);
    }

    // =====================================================================================
    // == 2. ORGANIZER-FOCUSED METHODS (Requires ownership, or Admin override)
    // =====================================================================================

    @Transactional
    public OrganizerEventResponse createEventForOrganizer(EventRequest eventRequest, MultipartFile imageFile) {
        User currentUser = getCurrentUser();
        Category category = categoryRepository.findById(eventRequest.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        City city = cityRepository.findById(eventRequest.getCityId()).orElseThrow(() -> new RuntimeException("City not found"));

        Event newEvent = modelMapper.map(eventRequest, Event.class);
        newEvent.setCategory(category);
        newEvent.setCity(city);
        newEvent.setOrganizer(currentUser);

        // *** MODIFIED PART ***
        if (imageFile != null && !imageFile.isEmpty()) {
            // The storeFile method now returns a full Cloudinary URL
            String imageUrl = imageService.storeFile(imageFile);
            newEvent.setEventImageUrl(imageUrl);
        }

        List<TicketType> ticketTypes = eventRequest.getTicketTypes().stream()
                .map(ttRequest -> {
                    TicketType ticketType = modelMapper.map(ttRequest, TicketType.class);
                    ticketType.setEvent(newEvent);
                    return ticketType;
                })
                .collect(Collectors.toList());
        newEvent.setTicketTypes(ticketTypes);

        try {
            String fullAddress = newEvent.getAddress() + ", " + newEvent.getCity().getName();
            LatLng coordinates = geocodingService.getCoordinates(fullAddress);
            if (coordinates != null) {
                newEvent.setLatitude(coordinates.lat);
                newEvent.setLongitude(coordinates.lng);
            }
        } catch (Exception e) {
            System.err.println("Could not geocode address during event creation. Saving event without coordinates.");
        }

        Event savedEvent = eventRepository.save(newEvent);
        return mapToOrganizerEventResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public Page<OrganizerEventResponse> getEventsForOrganizer(User organizer, Pageable pageable) {
        return eventRepository.findByOrganizer(organizer, pageable)
                .map(this::mapToOrganizerEventResponse);
    }

    @Transactional(readOnly = true)
    public OrganizerEventResponse getEventForOrganizerById(Long eventId) {
        Event event = findAndVerifyOwnership(eventId);
        return mapToOrganizerEventResponse(event);
    }

    @Transactional
    public OrganizerEventResponse updateEventForOrganizer(Long eventId, EventRequest eventRequest, MultipartFile imageFile) {
        Event existingEvent = findAndVerifyOwnership(eventId);

        modelMapper.map(eventRequest, existingEvent);
        Category category = categoryRepository.findById(eventRequest.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        City city = cityRepository.findById(eventRequest.getCityId()).orElseThrow(() -> new RuntimeException("City not found"));
        existingEvent.setCategory(category);
        existingEvent.setCity(city);

        // *** MODIFIED PART ***
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = imageService.storeFile(imageFile);
            existingEvent.setEventImageUrl(imageUrl);
        }

        // Complex logic to safely add, update, or remove ticket types
        Map<Long, TicketType> existingTicketTypesMap = existingEvent.getTicketTypes().stream()
                .collect(Collectors.toMap(TicketType::getId, tt -> tt));
        Set<Long> incomingTicketTypeIds = eventRequest.getTicketTypes().stream()
                .map(TicketTypeRequest::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        existingEvent.getTicketTypes().removeIf(ticketType -> !incomingTicketTypeIds.contains(ticketType.getId()));
        for (TicketTypeRequest ttRequest : eventRequest.getTicketTypes()) {
            if (ttRequest.getId() == null) { // New ticket type
                TicketType newTicketType = modelMapper.map(ttRequest, TicketType.class);
                newTicketType.setEvent(existingEvent);
                existingEvent.getTicketTypes().add(newTicketType);
            } else { // Existing ticket type
                TicketType ticketTypeToUpdate = existingTicketTypesMap.get(ttRequest.getId());
                if (ticketTypeToUpdate != null) {
                    modelMapper.map(ttRequest, ticketTypeToUpdate);
                }
            }
        }

        try {
            String fullAddress = existingEvent.getAddress() + ", " + existingEvent.getCity().getName();
            LatLng coordinates = geocodingService.getCoordinates(fullAddress);
            if (coordinates != null) {
                existingEvent.setLatitude(coordinates.lat);
                existingEvent.setLongitude(coordinates.lng);
            }
        } catch (Exception e) {
            System.err.println("Could not geocode address during event update. Saving event without coordinates.");
        }

        Event updatedEvent = eventRepository.save(existingEvent);
        return mapToOrganizerEventResponse(updatedEvent);
    }

    @Transactional
    public void deleteEventForOrganizer(Long eventId) {
        Event eventToDelete = findAndVerifyOwnership(eventId);
        eventRepository.delete(eventToDelete);
    }

    @Transactional(readOnly = true)
    public Page<OrganizerBookingResponse> getBookingsForEvent(Long eventId, Pageable pageable) {
        findAndVerifyOwnership(eventId);
        Page<Booking> bookings = bookingRepository.findByTicketType_Event_Id(eventId, pageable);
        return bookings.map(this::mapToOrganizerBookingResponse);
    }

    @Transactional
    public OrganizerTicketTypeResponse addTicketTypeToEvent(Long eventId, TicketTypeRequest ticketTypeRequest) {
        Event event = findAndVerifyOwnership(eventId);
        TicketType newTicketType = modelMapper.map(ticketTypeRequest, TicketType.class);
        newTicketType.setEvent(event);
        event.getTicketTypes().add(newTicketType);
        eventRepository.save(event);
        TicketType persistedTicketType = event.getTicketTypes().get(event.getTicketTypes().size() - 1);
        return mapToOrganizerTicketTypeResponse(persistedTicketType);
    }

    @Transactional
    public OrganizerTicketTypeResponse updateTicketType(Long eventId, Long ticketTypeId, TicketTypeRequest ticketTypeRequest) {
        Event event = findAndVerifyOwnership(eventId);
        TicketType ticketTypeToUpdate = event.getTicketTypes().stream()
                .filter(tt -> tt.getId().equals(ticketTypeId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("TicketType with id " + ticketTypeId + " not found in event " + eventId));
        modelMapper.map(ticketTypeRequest, ticketTypeToUpdate);
        eventRepository.save(event);
        return mapToOrganizerTicketTypeResponse(ticketTypeToUpdate);
    }

    @Transactional
    public void deleteTicketType(Long eventId, Long ticketTypeId) {
        Event event = findAndVerifyOwnership(eventId);
        long bookingCount = bookingRepository.countByTicketTypeId(ticketTypeId);
        if (bookingCount > 0) {
            throw new IllegalStateException(
                    "This ticket type cannot be deleted because " + bookingCount + " ticket(s) have already been sold. " +
                            "If you wish to stop sales, please edit the event and remove this ticket type or reduce its quantity."
            );
        }
        boolean removed = event.getTicketTypes().removeIf(tt -> tt.getId().equals(ticketTypeId));
        if (!removed) {
            throw new RuntimeException("TicketType with id " + ticketTypeId + " not found in event " + eventId);
        }
        eventRepository.save(event);
    }

    // =====================================================================================
    // == 3. ADMIN-ONLY METHODS
    // =====================================================================================

    @Transactional(readOnly = true)
    public Page<AdminEventResponse> getAllEventsForAdmin(Pageable pageable) {
        return eventRepository.findAll(pageable).map(this::mapToAdminEventResponse);
    }

    @Transactional(readOnly = true)
    public AdminEventResponse getEventForAdminById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return mapToAdminEventResponse(event);
    }

    // =====================================================================================
    // == 4. PRIVATE HELPERS & MAPPING METHODS
    // =====================================================================================

    private User getCurrentUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    private Event findAndVerifyOwnership(Long eventId) {
        User currentUser = getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        if (!isAdmin && !event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Forbidden: You do not have permission to access or modify this event.");
        }
        return event;
    }

    // *** DELETED createImageUrl() method ***

    private EventResponse mapToPublicEventResponse(Event event) {
        EventResponse response = modelMapper.map(event, EventResponse.class);
        response.setTicketTypes(event.getTicketTypes().stream()
                .map(this::mapToTicketTypeResponseWithAvailability)
                .collect(Collectors.toList()));
        // *** MODIFIED PART ***
        response.setEventImageUrl(event.getEventImageUrl()); // Directly use the URL from the entity
        return response;
    }

    private OrganizerEventResponse mapToOrganizerEventResponse(Event event) {
        OrganizerEventResponse response = modelMapper.map(event, OrganizerEventResponse.class);
        response.setTicketTypes(event.getTicketTypes().stream()
                .map(this::mapToOrganizerTicketTypeResponse)
                .collect(Collectors.toList()));
        // *** MODIFIED PART ***
        response.setEventImageUrl(event.getEventImageUrl()); // Directly use the URL from the entity
        return response;
    }

    private AdminEventResponse mapToAdminEventResponse(Event event) {
        AdminEventResponse response = modelMapper.map(event, AdminEventResponse.class);
        response.setTicketTypes(event.getTicketTypes().stream()
                .map(this::mapToOrganizerTicketTypeResponse)
                .collect(Collectors.toList()));
        // *** MODIFIED PART ***
        response.setEventImageUrl(event.getEventImageUrl()); // Directly use the URL from the entity
        return response;
    }

    private OrganizerBookingResponse mapToOrganizerBookingResponse(Booking booking) {
        User user = booking.getUser();
        TicketType ticketType = booking.getTicketType();
        UserSummaryResponse userSummary = new UserSummaryResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
        PurchasedTicketResponse ticketResponse = new PurchasedTicketResponse(ticketType.getId(), ticketType.getName(), ticketType.getPrice());
        return new OrganizerBookingResponse(booking.getId(), booking.getBookingDateTime(), ticketResponse, userSummary);
    }

    private OrganizerTicketTypeResponse mapToOrganizerTicketTypeResponse(TicketType ticketType) {
        long sold = bookingRepository.countByTicketTypeId(ticketType.getId());
        return new OrganizerTicketTypeResponse(ticketType.getId(), ticketType.getName(), ticketType.getPrice(),
                ticketType.getTotalAvailable(), sold);
    }

    private TicketTypeResponse mapToTicketTypeResponseWithAvailability(TicketType ticketType) {
        long sold = bookingRepository.countByTicketTypeId(ticketType.getId());
        int available = ticketType.getTotalAvailable() - (int) sold;
        TicketTypeResponse response = modelMapper.map(ticketType, TicketTypeResponse.class);
        response.setAvailability(calculateAvailabilityStatus(available, ticketType.getTotalAvailable()));
        int quantitySelectorThreshold = 10;
        response.setMaxPurchaseQuantity(Math.max(0, Math.min(available, quantitySelectorThreshold)));
        return response;
    }

    private AvailabilityStatus calculateAvailabilityStatus(int available, int total) {
        if (available <= 0) return AvailabilityStatus.SOLD_OUT;
        if (total == 0) return AvailabilityStatus.SOLD_OUT;
        double percentageLeft = (double) available / total;
        if (percentageLeft <= 0.1) return AvailabilityStatus.LIMITED;
        if (percentageLeft <= 0.4) return AvailabilityStatus.SELLING_FAST;
        return AvailabilityStatus.AVAILABLE;
    }
}