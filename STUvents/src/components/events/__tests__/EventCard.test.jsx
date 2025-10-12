import { it, expect, describe } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { EventCard } from '../EventCard';

// describe() groups related tests together.
describe('EventCard Component', () => {

  // We can define our fake data here so both tests can use it.
  const mockEvent = {
    id: 101,
    name: 'Campus Music Festival',
    category: { name: 'Music' },
    city: { name: 'New York' },
    eventDateTime: '2025-11-20T18:00:00',
    eventImageUrl: null, // We don't need a real image for these tests
  };

  // --- Test Case #1: Check for the text ---
  it('should render the visible text details correctly', () => {
    // ARRANGE & ACT: Render the component with our mock data.
    render(
      <BrowserRouter>
        <EventCard event={mockEvent} />
      </BrowserRouter>
    );

    // ASSERT: Check that all the text we expect to see is on the screen.
    expect(screen.getByText(/campus music festival/i)).toBeInTheDocument();
    expect(screen.getByText(/^music$/i)).toBeInTheDocument();
    expect(screen.getByText(/new york/i)).toBeInTheDocument();
    expect(screen.getByText(/november 20/i)).toBeInTheDocument();
  });

  // --- Test Case #2: Check the link ---
  it('should link to the correct event details page', () => {
    // ARRANGE & ACT: Render the component again for this isolated test.
    render(
      <BrowserRouter>
        <EventCard event={mockEvent} />
      </BrowserRouter>
    );

    // ASSERT: Check that the component is a link and that it goes to the right place.
    // 'getByRole' is a great way to find elements like links, buttons, headings, etc.
    const linkElement = screen.getByRole('link');
    
    // We check that the link has an 'href' attribute pointing to the correct URL.
    expect(linkElement).toHaveAttribute('href', '/events/101');
  });
});