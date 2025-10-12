// In file: src/pages/admin/AdminEventsFiltersPage.jsx (COMPLETE with full JSX)

import { useState, useEffect } from 'react';
import { Header } from '../../components/layout/Header';
// No longer importing 'api' directly
import * as adminService from '../../services/adminService'; // For Create, Update, Delete operations
import * as eventService from '../../services/eventService'; // For Read (fetching) operations
import './AdminEventsListPage.css'; // Reusing some table styles
import './AdminEventsFiltersPage.css';


export function AdminEventsFiltersPage() {
  // State for the lists of items
  const [categories, setCategories] = useState([]);
  const [cities, setCities] = useState([]);
  
  // State for the "add new" input fields
  const [newCategoryName, setNewCategoryName] = useState('');
  const [newCityName, setNewCityName] = useState('');

  // General loading and error state
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // State to control the edit modal
  const [editingItem, setEditingItem] = useState(null);

  // Function to fetch the initial data for both lists
  const fetchData = async () => {
    try {
      setLoading(true);
      // Use the eventService for fetching the lists
      const [catsRes, citiesRes] = await Promise.all([
        eventService.getAllCategories(),
        eventService.getAllCities()
      ]);
      setCategories(catsRes.data);
      setCities(citiesRes.data);
    } catch (err) {
      setError('Failed to load data. Please refresh the page.', err);
    } finally {
      setLoading(false);
    }
  };

  // Fetch data when the component first mounts
  useEffect(() => {
    fetchData();
  }, []);

  // Handler for adding a new category or city
  const handleAddNew = async (type, name, setNameCallback) => {
    if (!name.trim()) {
      alert('Name cannot be empty.');
      return;
    }
    try {
      // Use the adminService for creating new items
      if (type === 'category') {
        await adminService.createCategory(name);
      } else {
        await adminService.createCity(name);
      }
      setNameCallback(''); // Clear the input field
      fetchData(); // Refresh the list
    } catch (err) {
      alert(`Error adding item: ${err.response?.data?.message || 'Server error'}`);
    }
  };

  // Handler for deleting a category or city
  const handleDelete = async (type, id) => {
    if (!window.confirm(`Are you sure you want to delete this item? This may affect existing events.`)) {
      return;
    }
    try {
      // Use the adminService for deleting items
      if (type === 'category') {
        await adminService.deleteCategory(id);
      } else {
        await adminService.deleteCity(id);
      }
      fetchData(); // Refresh the list
    } catch (err) {
      alert(`Error deleting item: ${err.response?.data?.message || 'Server error'}`);
    }
  };

  // Handler for saving changes from the edit modal
  const handleUpdate = async () => {
    if (!editingItem || !editingItem.name.trim()) {
      alert('Name cannot be empty.');
      return;
    }
    try {
      // Use the adminService for updating items
      if (editingItem.endpoint === 'categories') {
        await adminService.updateCategory(editingItem.id, editingItem.name);
      } else {
        await adminService.updateCity(editingItem.id, editingItem.name);
      }
      setEditingItem(null); // Close the modal
      fetchData(); // Refresh the list
    } catch (err) {
      alert(`Error updating item: ${err.response?.data?.message || 'Server error'}`);
    }
  };

  if (loading) {
    return <p className="centered-message">Loading...</p>;
  }

  return (
    <div>
      <Header />
      <div className="container admin-events-page">
        <div className="admin-events-header">
          <h1>Manage Categories & Cities</h1>
        </div>
        
        {error && <p className="centered-message error-message">{error}</p>}

        <div className="filters-management-grid">
          {/* CATEGORIES SECTION */}
          <section className="management-section">
            <h2>Categories</h2>
            <form className="add-form" onSubmit={(e) => { e.preventDefault(); handleAddNew('category', newCategoryName, setNewCategoryName); }}>
              <input 
                type="text" 
                placeholder="New category name..."
                value={newCategoryName}
                onChange={(e) => setNewCategoryName(e.target.value)}
              />
              <button type="submit" className="button-primary">Add</button>
            </form>
            <div className="item-list">
              {categories.map(cat => (
                <div key={cat.id} className="list-item">
                  <span className="list-item-name">{cat.name}</span>
                  <div className="list-item-actions">
                    <button onClick={() => setEditingItem({ endpoint: 'categories', id: cat.id, name: cat.name })} className="button-secondary">Edit</button>
                    <button onClick={() => handleDelete('category', cat.id)} className="button-secondary button-danger">Delete</button>
                  </div>
                </div>
              ))}
            </div>
          </section>

          {/* CITIES SECTION */}
          <section className="management-section">
            <h2>Cities</h2>
            <form className="add-form" onSubmit={(e) => { e.preventDefault(); handleAddNew('city', newCityName, setNewCityName); }}>
              <input 
                type="text" 
                placeholder="New city name..."
                value={newCityName}
                onChange={(e) => setNewCityName(e.target.value)}
              />
              <button type="submit" className="button-primary">Add</button>
            </form>
            <div className="item-list">
              {cities.map(city => (
                <div key={city.id} className="list-item">
                  <span className="list-item-name">{city.name}</span>
                  <div className="list-item-actions">
                    <button onClick={() => setEditingItem({ endpoint: 'cities', id: city.id, name: city.name })} className="button-secondary">Edit</button>
                    <button onClick={() => handleDelete('city', city.id)} className="button-secondary button-danger">Delete</button>
                  </div>
                </div>
              ))}
            </div>
          </section>
        </div>
      </div>

      {/* EDIT MODAL - Renders conditionally */}
      {editingItem && (
        <div className="modal-overlay" onClick={() => setEditingItem(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Edit Item</h3>
            <input 
              type="text"
              className="add-form input" // Reusing styles from the add form
              value={editingItem.name}
              onChange={(e) => setEditingItem({ ...editingItem, name: e.target.value })}
            />
            <div className="modal-actions">
              <button onClick={() => setEditingItem(null)} className="button-secondary">Cancel</button>
              <button onClick={handleUpdate} className="button-primary">Save Changes</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}