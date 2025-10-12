// src/components/HeroSearchBar.jsx


import './HeroSearchBar.css';


export function HeroSearchBar({ cities, selectedCity, onCitySelect, onReset }) {
  
  const handleCityChange = (e) => {
    const newCity = e.target.value;
    onCitySelect(newCity);
  };

  const handleLocalReset = () => {
    onReset();
  };

  return (
 
    <div className="hero-search-bar">
      <div className="search-section where-section">
        <label htmlFor="city-select">Where</label>
        <select
          id="city-select"
          value={selectedCity} 
          onChange={handleCityChange}
        >
          <option value="" disabled>Select a city to find events</option>
          {cities.map(city => (
            <option key={city.id} value={city.name}>{city.name}</option>
          ))}
        </select>
      </div>
      <div className="search-actions">
        <button type="button" onClick={handleLocalReset} className="reset-button">
          Reset
        </button>
      </div>
    </div>
  );
}