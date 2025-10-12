import { useState } from 'react';
import './FilterModal.css';

export function PriceFilterModal({ currentRange, onApply, onClose }) {
  const [minPrice, setMinPrice] = useState(currentRange.min);
  const [maxPrice, setMaxPrice] = useState(currentRange.max);

  const handleApply = () => {
    onApply({ min: minPrice, max: maxPrice });
    onClose(); 
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="filter-modal-content" onClick={(e) => e.stopPropagation()}>
        <h3>Filter by Price</h3>
        <div className="price-inputs">
          <input
            type="number"
            placeholder="Min price"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
          />
          <span>-</span>
          <input
            type="number"
            placeholder="Max price"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
          />
        </div>
        <button onClick={handleApply} className="button-primary">Done</button>
      </div>
    </div>
  );
}