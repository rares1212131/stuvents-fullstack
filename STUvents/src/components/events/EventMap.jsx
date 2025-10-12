import { useState, useCallback } from 'react';
import { useJsApiLoader, GoogleMap, Marker, InfoWindow, MarkerClustererF } from '@react-google-maps/api';
import { Link } from 'react-router-dom';
import './EventMap.css';

const MAP_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;

export function EventMap({ events, center, hoveredEventId }) {
  // State for a single selected marker
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [selectedCluster, setSelectedCluster] = useState(null);

  const { isLoaded } = useJsApiLoader({
    id: 'google-map-script',
    googleMapsApiKey: MAP_API_KEY,
  });

  const handleClusterClick = useCallback((cluster) => {
    // Get all the markers inside the clicked cluster
    const markers = cluster.getMarkers();
    
    // Find all the corresponding event data for those markers
    const clusterEvents = events.filter(event => 
      markers.some(marker => 
        marker.getPosition().lat() === event.latitude && 
        marker.getPosition().lng() === event.longitude
      )
    );

    // If there's more than one event, set the selected cluster state
    if (clusterEvents.length > 1) {
      setSelectedEvent(null);
      setSelectedCluster({
        position: cluster.getCenter().toJSON(),
        events: clusterEvents,
      });
    } else if (clusterEvents.length === 1) {
      setSelectedCluster(null);
      setSelectedEvent(clusterEvents[0]);
    }
  }, [events]);


  if (!isLoaded) {
    return <div>Loading Map...</div>;
  }

  const defaultIcon = {
    url: '/icons/marker-default.svg',
    scaledSize: new window.google.maps.Size(40, 40),
  };

  const hoverIcon = {
    url: '/icons/marker-hover.svg',
    scaledSize: new window.google.maps.Size(50, 50),
  };

  return (
    <GoogleMap
      mapContainerStyle={{ width: '100%', height: '100%' }}
      center={center}
      zoom={13}
      options={{
        disableDefaultUI: true,
        zoomControl: true,
      }}
      onClick={() => {
        setSelectedEvent(null);
        setSelectedCluster(null);
      }}
    >
      <MarkerClustererF 
        onClick={handleClusterClick} // <<< Attach our new click handler to the clusterer
      >
        {(clusterer) =>
          events.map((event) =>
            event.latitude && event.longitude ? (
              <Marker
                key={event.id}
                position={{ lat: event.latitude, lng: event.longitude }}
                icon={hoveredEventId === event.id ? hoverIcon : defaultIcon}
                zIndex={hoveredEventId === event.id ? 100 : 1}
                onClick={() => {
                  setSelectedCluster(null); // Close cluster window if open
                  setSelectedEvent(event);
                }}
                clusterer={clusterer}
              />
            ) : null
          )
        }
      </MarkerClustererF>

      {selectedEvent && (
        <InfoWindow
          position={{ lat: selectedEvent.latitude, lng: selectedEvent.longitude }}
          onCloseClick={() => setSelectedEvent(null)}
        >
          <div className="map-infowindow">
            <img src={selectedEvent.eventImageUrl || "/images/placeholder-image.avif"} alt={selectedEvent.name} />
            <div className="infowindow-content">
              <h4>{selectedEvent.name}</h4>
              <Link to={`/events/${selectedEvent.id}`} className="infowindow-link">
                View Details
              </Link>
            </div>
          </div>
        </InfoWindow>
      )}

      {selectedCluster && (
        <InfoWindow
          position={selectedCluster.position}
          onCloseClick={() => setSelectedCluster(null)}
        >
          <div className="map-cluster-infowindow">
            <h4>Multiple Events</h4>
            <ul>
              {selectedCluster.events.map(event => (
                <li key={event.id}>
                  <Link to={`/events/${event.id}`}>{event.name}</Link>
                </li>
              ))}
            </ul>
          </div>
        </InfoWindow>
      )}
    </GoogleMap>
  );
}