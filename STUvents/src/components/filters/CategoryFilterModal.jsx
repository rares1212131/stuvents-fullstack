import { useState } from 'react';
import './FilterModal.css';

export function CategoryFilterModal({ allCategories, selectedCategories, onApply, onClose }) {
  const [checkedCategories, setCheckedCategories] = useState(new Set(selectedCategories));

  const handleCheckboxChange = (categoryName) => {
    const newChecked = new Set(checkedCategories);
    if (newChecked.has(categoryName)) {
      newChecked.delete(categoryName);
    } else {
      newChecked.add(categoryName);
    }
    setCheckedCategories(newChecked);
  };

  const handleApply = () => {
    onApply(Array.from(checkedCategories));
  };
  
  const handleClear = () => {
    setCheckedCategories(new Set());
    onApply([]); // Apply immediately
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="filter-modal-content" onClick={(e) => e.stopPropagation()}>
        <h3>Filter by Category</h3>
        <div className="category-list">
          {allCategories.map(cat => (
            <label key={cat.id}>
              <input
                type="checkbox"
                checked={checkedCategories.has(cat.name)}
                onChange={() => handleCheckboxChange(cat.name)}
              />
              {cat.name}
            </label>
          ))}
        </div>
        <div className="modal-actions">
           <button onClick={handleClear} className="button-secondary">Clear</button>
           <button onClick={handleApply} className="button-primary">Apply</button>
        </div>
      </div>
    </div>
  );
}