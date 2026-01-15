import { useState } from 'react';
import './TicketTypeModal.css';
import './RoleManagementModal.css';

export function RoleManagementModal({ user, allRoles, onClose, onSave }) {
  const [selectedRoles, setSelectedRoles] = useState(new Set(user.roles || []));

  const handleCheckboxChange = (role) => {
    const newSelectedRoles = new Set(selectedRoles);
    if (newSelectedRoles.has(role)) {
      newSelectedRoles.delete(role);
    } else {
      newSelectedRoles.add(role);
    }
    setSelectedRoles(newSelectedRoles);
  };

  const handleSubmit = () => {
    onSave(user.id, Array.from(selectedRoles));
    onClose();
  };
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Manage Roles for {user.firstName} {user.lastName}</h2>
          <button onClick={onClose} className="modal-close-button">&times;</button>
        </div>
        <div className="modal-body">
          <p className="role-selection-prompt">Select the roles this user should have.</p>
          <div className="roles-container">
            {allRoles.map(role => (
              <label key={role} className="role-label">
                <input
                  type="checkbox"
                  className="role-checkbox"
                  checked={selectedRoles.has(role)}
                  onChange={() => handleCheckboxChange(role)}
                />
                <span className="role-name">{role.replace('ROLE_', '')}</span>
              </label>
            ))}
          </div>
        </div>
        <div className="modal-actions">
          <button onClick={onClose} className="button-secondary">Cancel</button>
          <button onClick={handleSubmit} className="button-primary">Save Changes</button>
        </div>
      </div>
    </div>
  );
}