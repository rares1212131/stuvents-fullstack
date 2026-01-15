import { it, expect, describe } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { EventCard } from '../EventCard';

describe('EventCard Component', () => {

  const mockEvent = {
    id: 101,
    name: 'Campus Music Festival',
    category: { name: 'Music' },
    city: { name: 'New York' },
    eventDateTime: '2025-11-20T18:00:00',
    eventImageUrl: null, 
  };

  it('should render the visible text details correctly', () => {
    render(
      <BrowserRouter>
        <EventCard event={mockEvent} />
      </BrowserRouter>
    );

    expect(screen.getByText(/campus music festival/i)).toBeInTheDocument();
    expect(screen.getByText(/^music$/i)).toBeInTheDocument();
    expect(screen.getByText(/new york/i)).toBeInTheDocument();
    expect(screen.getByText(/november 20/i)).toBeInTheDocument();
  });
  it('should link to the correct event details page', () => {
    render(
      <BrowserRouter>
        <EventCard event={mockEvent} />
      </BrowserRouter>
    );

    const linkElement = screen.getByRole('link');
    expect(linkElement).toHaveAttribute('href', '/events/101');
  });
});