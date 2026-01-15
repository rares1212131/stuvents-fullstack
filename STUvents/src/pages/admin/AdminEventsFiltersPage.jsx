
import { useState, useEffect } from 'react';
import { Header } from '../../components/layout/Header';
import * as adminService from '../../services/adminService';  
import * as eventService from '../../services/eventService'; 
import './AdminEventsListPage.css'; 
import './AdminEventsFiltersPage.css';


export function AdminEventsFiltersPage() {
  const [categories, setCategories] = useState([]);
  const [cities, setCities] = useState([]);
  const [newCategoryName, setNewCategoryName] = useState('');
  const [newCityName, setNewCityName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editingItem, setEditingItem] = useState(null);
  const fetchData = async () => {
    try {
      setLoading(true);
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
  useEffect(() => {
    fetchData();
  }, []);
  const handleAddNew = async (type, name, setNameCallback) => {
    if (!name.trim()) {
      alert('Name cannot be empty.');
      return;
    }
    try {
      if (type === 'category') {
        await adminService.createCategory(name);
      } else {
        await adminService.createCity(name);
      }
      setNameCallback(''); 
      fetchData(); 
    } catch (err) {
      alert(`Error adding item: ${err.response?.data?.message || 'Server error'}`);
    }
  };
  const handleDelete = async (type, id) => {
    if (!window.confirm(`Are you sure you want to delete this item? This may affect existing events.`)) {
      return;
    }
    try {
      if (type === 'category') {
        await adminService.deleteCategory(id);
      } else {
        await adminService.deleteCity(id);
      }
      fetchData(); 
    } catch (err) {
      alert(`Error deleting item: ${err.response?.data?.message || 'Server error'}`);
    }
  };

  const handleUpdate = async () => {
    if (!editingItem || !editingItem.name.trim()) {
      alert('Name cannot be empty.');
      return;
    }
    try {
      if (editingItem.endpoint === 'categories') {
        await adminService.updateCategory(editingItem.id, editingItem.name);
      } else {
        await adminService.updateCity(editingItem.id, editingItem.name);
      }
      setEditingItem(null); 
      fetchData(); 
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
      {editingItem && (
        <div className="modal-overlay" onClick={() => setEditingItem(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Edit Item</h3>
            <input 
              type="text"
              className="add-form input"
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