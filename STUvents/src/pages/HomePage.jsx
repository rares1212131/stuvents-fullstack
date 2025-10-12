// In file: src/pages/HomePage.jsx (REFACTORED WITH URL STATE)

import { useState, useEffect, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom'; // <<< 1. Import useSearchParams
import * as eventService from '../services/eventService'; 

import { Header } from '../components/layout/Header';
import { EventCard } from '../components/events/EventCard';
import { HeroSearchBar } from '../components/layout/HeroSearchBar';
import { SecondaryFilters } from '../components/filters/SecondaryFilters';
import { EventMap } from '../components/events/EventMap';
import './HomePage.css';

export function HomePage() {
  // <<< 2. Get the tools to read and write to the URL's query string
  const [searchParams, setSearchParams] = useSearchParams();

  // The component's internal state is now DRIVEN BY the URL.
  const [allEvents, setAllEvents] = useState([]);
  const [initialEvents, setInitialEvents] = useState([]);
  const [categories, setCategories] = useState([]);
  const [cities, setCities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [hoveredEventId, setHoveredEventId] = useState(null);

  // <<< 3. Derive state from the URL search parameters instead of local state
  // We use `useMemo` so these values are only recalculated when the searchParams string changes.
  const activeCity = useMemo(() => searchParams.get('city') || '', [searchParams]);
  const secondaryFilters = useMemo(() => ({
    priceRange: {
      min: searchParams.get('minPrice') || '',
      max: searchParams.get('maxPrice') || '',
    },
    // `getAll` is used for parameters that can appear multiple times (e.g., &category=A&category=B)
    selectedCategories: searchParams.getAll('category') || [],
  }), [searchParams]);

  // isMapViewActive is now a derived value. If there's a city in the URL, the map is active.
  const isMapViewActive = !!activeCity;

  // This useEffect for fetching dropdowns remains the same
  useEffect(() => {
    const fetchDropdownData = async () => {
      try {
        const [categoriesRes, citiesRes] = await Promise.all([
          eventService.getAllCategories(), 
          eventService.getAllCities(),      
        ]);
        setCategories(categoriesRes.data);
        setCities(citiesRes.data);
      } catch (error) {
        console.error("Failed to fetch filter options:", error);
      }
    };
    fetchDropdownData();
  }, []);

  // This useEffect for fetching initial "trending" events remains the same
  useEffect(() => {
    const fetchInitialEvents = async () => {
      setLoading(true);
      try {
        const response = await eventService.getInitialEvents(); 
        setInitialEvents(response.data.content);
      } catch (error) {
        console.error("Failed to fetch initial events:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchInitialEvents();
  }, []);
  
  // <<< 4. The main data fetching effect now depends on `activeCity` (derived from the URL)
  useEffect(() => {
    // If there is no city selected in the URL, we don't need to fetch searched events.
    if (!activeCity) {
      setAllEvents([]); // Clear any previous search results
      return;
    }

    const fetchSearchedEvents = async () => {
      setLoading(true);
      try {
        const response = await eventService.getEventsByCity(activeCity); 
        setAllEvents(response.data.content);
      } catch (error) {
        console.error("Failed to fetch events:", error);
        setAllEvents([]);
      } finally {
        setLoading(false);
      }
    };

    fetchSearchedEvents();
  }, [activeCity]); // This effect re-runs ONLY when the city in the URL changes.


  // <<< 5. All handler functions now UPDATE THE URL instead of local state.
  // Updating the URL will cause the component to re-render and the derived state (step 3) to update.

  const handleCitySelect = (cityName) => {
    if (cityName) {
      // Set the city in the URL, which will trigger the fetch effect.
      // We create a new URLSearchParams object to clear any old filters.
      setSearchParams({ city: cityName });
    }
  };

  const handleSecondaryFilterChange = (filterName, value) => {
    // Create a new URLSearchParams object based on the current URL.
    const newSearchParams = new URLSearchParams(searchParams);

    if (filterName === 'priceRange') {
      newSearchParams.set('minPrice', value.min || '');
      newSearchParams.set('maxPrice', value.max || '');
    }
    if (filterName === 'selectedCategories') {
      // Delete all existing category params first
      newSearchParams.delete('category');
      // Then add one for each selected category
      value.forEach(cat => newSearchParams.append('category', cat));
    }
    setSearchParams(newSearchParams);
  };

  const handleReset = () => {
    // To reset, we just set the search params to an empty object.
    setSearchParams({});
  };
  
  // This filtering logic remains largely the same, but it now uses the URL-derived state.
  const filteredEvents = useMemo(() => {
    const sourceList = isMapViewActive ? allEvents : initialEvents;
    if (!isMapViewActive) return sourceList;

    return sourceList.filter(event => {
      if (secondaryFilters.selectedCategories.length > 0 && !secondaryFilters.selectedCategories.includes(event.category.name)) {
        return false;
      }
      const minPrice = event.ticketTypes.length > 0 ? Math.min(...event.ticketTypes.map(t => t.price)) : 0;
      if (secondaryFilters.priceRange.min && minPrice < secondaryFilters.priceRange.min) return false;
      if (secondaryFilters.priceRange.max && minPrice > secondaryFilters.priceRange.max) return false;
      return true;
    });
  }, [allEvents, initialEvents, secondaryFilters, isMapViewActive]);

  // This logic remains the same.
  const mapCenter = useMemo(() => {
    const listToUse = isMapViewActive ? filteredEvents : [];
    const firstEventWithCoords = listToUse.find(e => e.latitude != null && e.longitude != null);
    if (firstEventWithCoords) return { lat: firstEventWithCoords.latitude, lng: firstEventWithCoords.longitude };
    return { lat: 46.7712, lng: 23.6236 };
  }, [filteredEvents, isMapViewActive]);

  return (
    <div>
      <Header onLogoClick={handleReset} />
      <main className="home-page-content">
        <div className="hero-section">
          <div className="container">
            <h1>Find Tickets Right Here</h1>
            <HeroSearchBar
              cities={cities}
              selectedCity={activeCity} // <<< Pass the URL-derived state down as a prop
              onCitySelect={handleCitySelect}
              onReset={handleReset}
            />
          </div>
        </div>

        <div className={`container results-container ${isMapViewActive ? 'split-view-active' : ''}`}>
          <div className="results-list">
            {isMapViewActive && (
              <SecondaryFilters
                categories={categories}
                currentFilters={secondaryFilters} // <<< Pass the URL-derived state down as a prop
                onFilterChange={handleSecondaryFilterChange}
              />
            )}
            {loading && <p className="loading-text">Loading events...</p>}
            {!loading && isMapViewActive && allEvents.length === 0 && (
              <p className="centered-message">No events found for this city. Try another location.</p>
            )}
            <div className="events-grid">
              {(isMapViewActive ? filteredEvents : initialEvents).map(event => (
                <EventCard key={event.id} event={event} onMouseEnter={() => setHoveredEventId(event.id)} onMouseLeave={() => setHoveredEventId(null)} />
              ))}
            </div>
          </div>

          {isMapViewActive && !loading && (
            <div className="results-map">
              <EventMap events={filteredEvents} center={mapCenter} hoveredEventId={hoveredEventId} />
            </div>
          )}
        </div>
      </main>
    </div>
  );
}