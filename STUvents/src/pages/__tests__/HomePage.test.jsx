import { it, expect, describe, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import * as eventService from '../../services/eventService';
import { AuthContext } from '../../context/AuthContext';
import { HomePage } from '../HomePage';

vi.mock('../../services/eventService');

const renderHomePage = (initialRoute = '/') => {
  const mockAuthContext = {
    isAuthenticated: false,
    user: null,
    logout: vi.fn(),
  };

  render(
    <MemoryRouter initialEntries={[initialRoute]}>
      <AuthContext.Provider value={mockAuthContext}>
        <HomePage />
      </AuthContext.Provider>
    </MemoryRouter>
  );
};


describe('HomePage Component', () => {

  // <<< FIX #1: Add the ticketTypes property here >>>
  const mockInitialEvents = {
    data: {
      content: [
        { id: 1, name: 'Trending Event One', category: { name: 'A' }, city: { name: 'B' }, eventDateTime: '2025-01-01', ticketTypes: [] },
        { id: 2, name: 'Trending Event Two', category: { name: 'C' }, city: { name: 'D' }, eventDateTime: '2025-01-02', ticketTypes: [] },
      ],
    },
  };

  // <<< FIX #2: Add the ticketTypes property here as well >>>
  const mockSearchedEvents = {
    data: {
      content: [
        { id: 3, name: 'Searched Event Alpha', category: { name: 'E' }, city: { name: 'F' }, eventDateTime: '2025-02-01', ticketTypes: [] },
      ],
    },
  };
  
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(eventService.getAllCategories).mockResolvedValue({ data: [] });
    vi.mocked(eventService.getAllCities).mockResolvedValue({ data: [] });
  });

  it('should fetch and display initial "trending" events on first load', async () => {
    vi.mocked(eventService.getInitialEvents).mockResolvedValue(mockInitialEvents);
    renderHomePage();

    // The test logic here doesn't need to change.
    expect(screen.getByText(/loading events/i)).toBeInTheDocument();
    expect(await screen.findByText(/trending event one/i)).toBeInTheDocument();
    expect(await screen.findByText(/trending event two/i)).toBeInTheDocument();
    expect(eventService.getInitialEvents).toHaveBeenCalledTimes(1);
  });
  
  it('should fetch searched events when a city is in the URL', async () => {
    vi.mocked(eventService.getInitialEvents).mockResolvedValue({ data: { content: [] } });
    vi.mocked(eventService.getEventsByCity).mockResolvedValue(mockSearchedEvents);
    renderHomePage('/?city=NewYork');

    // The test logic here also doesn't need to change.
    expect(await screen.findByText(/searched event alpha/i)).toBeInTheDocument();
    expect(eventService.getEventsByCity).toHaveBeenCalledTimes(1);
    expect(eventService.getEventsByCity).toHaveBeenCalledWith('NewYork');
  });
});