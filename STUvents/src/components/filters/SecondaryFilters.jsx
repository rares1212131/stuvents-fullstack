import { useState } from 'react';
import { PriceFilterModal } from './PriceFilterModal';
import { CategoryFilterModal } from './CategoryFilterModal';
import './SecondaryFIlters.css';

export function SecondaryFilters({ categories, currentFilters, onFilterChange }) {
  const [activeModal, setActiveModal] = useState(null);

  const handlePriceApply = (priceRange) => {
    onFilterChange('priceRange', priceRange);
    setActiveModal(null);
  };

  const handleCategoryApply = (selectedCategories) => {
    onFilterChange('selectedCategories', selectedCategories);
    setActiveModal(null);
  };
  return (
    <div className="secondary-filters">
      <button onClick={() => setActiveModal('price')} className="filter-button">Price</button>
      <button onClick={() => setActiveModal('category')} className="filter-button">Category</button>
      {activeModal === 'price' && (
        <PriceFilterModal
          currentRange={currentFilters.priceRange}
          onApply={handlePriceApply}
          onClose={() => setActiveModal(null)}
        />
      )}
      {activeModal === 'category' && (
        <CategoryFilterModal
          allCategories={categories}
          selectedCategories={currentFilters.selectedCategories}
          onApply={handleCategoryApply}
          onClose={() => setActiveModal(null)}
        />
      )}
    </div>
  );
}