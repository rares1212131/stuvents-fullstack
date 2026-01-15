
import { useState, useEffect, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom'; 
import * as eventService from '../services/eventService'; 

import { Header } from '../components/layout/Header';
import { EventCard } from '../components/events/EventCard';
import { HeroSearchBar } from '../components/layout/HeroSearchBar';
import { SecondaryFilters } from '../components/filters/SecondaryFilters';
import { EventMap } from '../components/events/EventMap';
import './HomePage.css';

export function HomePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [allEvents, setAllEvents] = useState([]);
  const [initialEvents, setInitialEvents] = useState([]);
  const [categories, setCategories] = useState([]);
  const [cities, setCities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [hoveredEventId, setHoveredEventId] = useState(null);

  const activeCity = useMemo(() => searchParams.get('city') || '', [searchParams]);
  const secondaryFilters = useMemo(() => ({
    priceRange: {
      min: searchParams.get('minPrice') || '',
      max: searchParams.get('maxPrice') || '',
    },

    selectedCategories: searchParams.getAll('category') || [],
  }), [searchParams]);

  const isMapViewActive = !!activeCity;

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

  useEffect(() => {
    if (!activeCity) {
      setAllEvents([]); 
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
  }, [activeCity]); 


  const handleCitySelect = (cityName) => {
    if (cityName) {
      setSearchParams({ city: cityName });
    }
  };

  const handleSecondaryFilterChange = (filterName, value) => {
    const newSearchParams = new URLSearchParams(searchParams);

    if (filterName === 'priceRange') {
      newSearchParams.set('minPrice', value.min || '');
      newSearchParams.set('maxPrice', value.max || '');
    }
    if (filterName === 'selectedCategories') {
      newSearchParams.delete('category');
      value.forEach(cat => newSearchParams.append('category', cat));
    }
    setSearchParams(newSearchParams);
  };

  const handleReset = () => {
    setSearchParams({});
  };
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
              selectedCity={activeCity} 
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
                currentFilters={secondaryFilters} 
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