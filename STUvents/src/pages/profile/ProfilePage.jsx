
import { useState, useEffect } from 'react';
import { Header } from '../../components/layout/Header';
import { useAuth } from '../../context/AuthContext';
import * as userService from '../../services/userService'; 
import * as organizerService from '../../services/organizerService'; 
import './ProfilePage.css';

const DEFAULT_AVATAR = "/icons/default-avatar.svg"; 

export function ProfilePage() {
  const { user, refreshUser } = useAuth(); 
  
  const [formData, setFormData] = useState({ firstName: '', lastName: '' });
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [applicationStatus, setApplicationStatus] = useState(null);
  const [loadingStatus, setLoadingStatus] = useState(true);

  useEffect(() => {
    if (user) {
      setFormData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
      });
    }
  }, [user]);

  useEffect(() => {
    if (user && !user.roles.includes('ROLE_ORGANIZER')) {
      const checkStatus = async () => {
        setLoadingStatus(true);
        try {
          const response = await organizerService.getMyApplicationStatus();
          setApplicationStatus(response.data ? response.data.status : 'NOT_APPLIED');
        } catch (err) {
          if (err.response && err.response.status === 404) {
            setApplicationStatus('NOT_APPLIED');
          } else {
            console.error("Failed to fetch application status", err);
            setApplicationStatus('ERROR');
          }
        } finally {
          setLoadingStatus(false);
        }
      };
      checkStatus();
    } else {
      setLoadingStatus(false);
    }
  }, [user]);

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });
  
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setSelectedFile(file);
      setPreviewUrl(URL.createObjectURL(file));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccessMessage('');

    const formDataToSend = new FormData();
    const userUpdateData = {
        firstName: formData.firstName,
        lastName: formData.lastName
    };
    formDataToSend.append('userUpdate', new Blob([JSON.stringify(userUpdateData)], { type: 'application/json' }));
    if (selectedFile) {
      formDataToSend.append('image', selectedFile);
    }

    try {

      await userService.updateUserProfile(user.id, formDataToSend);
      await refreshUser();
      
      setSuccessMessage('Profile updated successfully!');
      setSelectedFile(null);
      setPreviewUrl('');
      
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      console.error('Profile update error:', err);
      setError('Failed to update profile. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const renderApplicationStatus = () => {
    if (loadingStatus) {
      return <p>Loading status...</p>;
    }
    
    switch (applicationStatus) {
      case 'PENDING':
        return <p style={{fontWeight: 'bold'}}>Your application is currently pending review.</p>;
      case 'APPROVED':
        return <p style={{fontWeight: 'bold', color: 'green'}}>Congratulations! Your application was approved. Please log out and log back in to access your Organizer Dashboard.</p>;
      case 'DENIED':
        return <p style={{fontWeight: 'bold', color: 'red'}}>Your application was not approved at this time.</p>;
      case 'ERROR':
        return <p className="error-message">Could not load application status. Please try refreshing.</p>;
      case 'NOT_APPLIED':
      default:
        return <p>You have not applied to be an organizer. You can apply from the dropdown menu in the header.</p>;
    }
  };
  if (!user) {
    return <div>Loading profile...</div>;
  }

  return (
    <div>
      <Header />
      <div className="profile-page-container">
        <form className="profile-form" onSubmit={handleSubmit}>
          <h1>My Profile</h1>
          
          {successMessage && <div className="message success">{successMessage}</div>}
          {error && <div className="message error">{error}</div>}

          <div className="form-group" style={{ alignItems: 'center' }}>
            <img 
              src={previewUrl || user.profilePictureUrl || DEFAULT_AVATAR} 
              alt="Profile" 
              style={{ width: '120px', height: '120px', borderRadius: '50%', objectFit: 'cover', border: '3px solid #495057' }}
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <div className="non-editable-field">{user.email}</div>
          </div>
          <div className="form-group">
            <label>First Name</label>
            <input 
              type="text" 
              name="firstName" 
              value={formData.firstName} 
              onChange={handleChange} 
              required 
            />
          </div>
          <div className="form-group">
            <label>Last Name</label>
            <input 
              type="text" 
              name="lastName" 
              value={formData.lastName} 
              onChange={handleChange} 
              required 
            />
          </div>
          <div className="form-group">
            <label>Change Profile Picture</label>
            <input 
              type="file" 
              name="image" 
              accept="image/png, image/jpeg, image/webp, image/avif" 
              onChange={handleFileChange} 
            />
          </div>
          <div className="profile-actions">
            <button type="submit" className="button-primary" disabled={loading}>
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
        {!user.roles.includes('ROLE_ORGANIZER') && !user.roles.includes('ROLE_ADMIN') && (
            <div className="profile-form" style={{marginTop: '2rem'}}>
                <h2>Organizer Application Status</h2>
                <div style={{ padding: '1rem 0', textAlign: 'center' }}>
                  {renderApplicationStatus()}
                </div>
            </div>
        )}
      </div>
    </div>
  );
}