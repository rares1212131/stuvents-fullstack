

import { useState, useEffect } from 'react';
import { Header } from '../../components/layout/Header';
import * as adminService from '../../services/adminService'; 
import './AdminEventsListPage.css'; 

export function AdminApplicationsPages() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const fetchApplications = async () => {
    try {
      setLoading(true);
      const response = await adminService.getPendingApplications();
      setApplications(response.data);
    } catch (err) {
      setError('Failed to fetch applications.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchApplications();
  }, []);

  const handleApprove = async (id) => {
    try {
      await adminService.approveApplication(id);
      setMessage('Application approved successfully!');
      fetchApplications();
    } catch (err) {
      setError('Failed to approve application.');
      console.error(err);
    }
  };

  const handleDeny = async (id) => {
    try {
      await adminService.denyApplication(id);
      setMessage('Application denied.');
      fetchApplications(); 
    } catch (err) {
      setError('Failed to deny application.');
      console.error(err);
    }
  };

  return (
    <div>
      <Header />
      <div className="container admin-events-page">
        <div className="admin-events-header">
          <h1>Pending Organizer Applications</h1>
        </div>

        {loading && <p className="centered-message">Loading applications...</p>}
        {error && <p className="centered-message error-message">{error}</p>}
        {message && <p className="centered-message" style={{ color: 'green' }}>{message}</p>}

        {!loading && !error && (
          applications.length > 0 ? (
            <table className="events-list-table">
              <thead>
                <tr>
                  <th>Applicant Name</th>
                  <th>Email</th>
                  <th>Reason</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {applications.map(app => (
                  <tr key={app.id}>
                    <td>{app.userFirstName} {app.userLastName}</td>
                    <td>{app.userEmail}</td>
                    <td style={{ maxWidth: '400px' }}>{app.reason}</td>
                    <td className="event-actions">
                      <button onClick={() => handleApprove(app.id)} className="button-primary" style={{ marginRight: '0.5rem' }}>Approve</button>
                      <button onClick={() => handleDeny(app.id)} className="button-secondary button-danger">Deny</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p className="centered-message">No pending applications found.</p>
          )
        )}
      </div>
    </div>
  );
}