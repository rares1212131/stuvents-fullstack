// In file: src/pages/admin/AdminUsersListPage.jsx (COMPLETE FILE)

import { useState, useEffect } from 'react';
import { Header } from '../../components/layout/Header';
import { RoleManagementModal } from '../../components/admin/RoleManagementModal';
import * as adminService from '../../services/adminService';
import './AdminEventsListPage.css';

export function AdminUsersListPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedUser, setSelectedUser] = useState(null);

  const ALL_PLATFORM_ROLES = ['ROLE_USER', 'ROLE_ORGANIZER', 'ROLE_ADMIN'];

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await adminService.getAllUsers();
      setUsers(response.data);
    } catch (err) {
      setError('Failed to fetch users.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleRoleChange = async (userId, roles) => {
    try {
      await adminService.updateUserRoles(userId, roles);
      alert('Roles updated successfully!');
      fetchUsers();
    } catch (err) {
      alert('Failed to update roles. Please check the console.');
      console.error(err);
    }
  };

  // ★★★ 1. ADD THIS NEW HANDLER FUNCTION ★★★
  const handleDeleteUser = async (userId, userEmail) => {
    // Add a confirmation prompt to prevent accidental deletions
    if (!window.confirm(`Are you sure you want to permanently delete the user: ${userEmail}? This action cannot be undone.`)) {
      return; // If the admin clicks "Cancel", stop the function
    }

    try {
      // Call the new service function we created in Step 1
      await adminService.deleteUser(userId);
      alert('User deleted successfully!');
      // Refresh the list of users to remove the deleted one from the view
      fetchUsers();
    } catch (err) {
      alert('Failed to delete user. The user might be an organizer of an event.');
      console.error(err);
    }
  };

  return (
    <div>
      <Header />
      <div className="container admin-events-page">
        <div className="admin-events-header">
          <h1>Manage Users</h1>
        </div>

        {loading && <p className="centered-message">Loading users...</p>}
        {error && <p className="centered-message error-message">{error}</p>}

        {!loading && !error && (
          <table className="events-list-table">
            <thead>
              <tr>
                <th>User ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Roles</th>
                <th style={{width: '300px'}}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map(user => (
                <tr key={user.id}>
                  <td>{user.id}</td>
                  <td>{user.firstName} {user.lastName}</td>
                  <td>{user.email}</td>
                  <td>{user.roles.map(r => r.replace('ROLE_', '')).join(', ')}</td>
                  <td className="event-actions">
                    <button onClick={() => setSelectedUser(user)} className="button-secondary">Manage Roles</button>
                    
                    {/* ★★★ 2. ADD THE DELETE BUTTON HERE ★★★ */}
                    <button onClick={() => handleDeleteUser(user.id, user.email)} className="button-secondary button-danger">
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {selectedUser && (
        <RoleManagementModal
          user={selectedUser}
          allRoles={ALL_PLATFORM_ROLES}
          onClose={() => setSelectedUser(null)}
          onSave={handleRoleChange}
        />
      )}
    </div>
  );
}