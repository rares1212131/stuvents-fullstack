import { it, expect, describe, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { AuthContext } from '../../../context/AuthContext';
import { TicketPurchaseCard } from '../TicketPurchaseCard';

// This helper function stays the same.
const renderComponentWithProviders = (ticketTypes, isAuthenticated = true) => {
  const mockOnPurchaseInitiate = vi.fn();

  render(
    <BrowserRouter>
      <AuthContext.Provider value={{ isAuthenticated }}>
        <TicketPurchaseCard
          ticketTypes={ticketTypes}
          onPurchaseInitiate={mockOnPurchaseInitiate}
        />
      </AuthContext.Provider>
    </BrowserRouter>
  );
  
  return { mockOnPurchaseInitiate };
};


describe('TicketPurchaseCard Component', () => {

  // Test Case #1 (Stays the same)
  it('should render with the purchase button disabled if the user is logged in', () => {
    const mockTickets = [
      { id: 1, name: 'General Admission', price: 25.00, availability: 'AVAILABLE', maxPurchaseQuantity: 10 }
    ];
    renderComponentWithProviders(mockTickets, true);
    const purchaseButton = screen.getByRole('button', { name: /purchase ticket/i });
    expect(purchaseButton).toBeDisabled();
  });

  // Test Case #2 (Stays the same)
  it('should enable the purchase button after a user clicks a ticket option', async () => {
    const mockTickets = [
      { id: 1, name: 'General Admission', price: 25.00, availability: 'AVAILABLE', maxPurchaseQuantity: 10 }
    ];
    const user = userEvent.setup();
    renderComponentWithProviders(mockTickets, true);

    const purchaseButton = screen.getByRole('button', { name: /purchase ticket/i });
    const ticketOption = screen.getByText(/general admission/i);
    await user.click(ticketOption);

    expect(purchaseButton).toBeEnabled();
  });

  // Test Case #3 (Stays the same)
  it('should show a "Log In to Purchase" link if the user is not authenticated', () => {
    const mockTickets = [
      { id: 1, name: 'General Admission', price: 25.00, availability: 'AVAILABLE', maxPurchaseQuantity: 10 }
    ];
    renderComponentWithProviders(mockTickets, false);

    const loginLink = screen.getByRole('link', { name: /log in to purchase/i });
    expect(loginLink).toBeInTheDocument();
    
    const purchaseButton = screen.queryByRole('button', { name: /purchase ticket/i });
    expect(purchaseButton).not.toBeInTheDocument();
  });

  // <<< NEW TEST CASE #4: THE FULL FLOW >>>
  it('should call onPurchaseInitiate with the correct ticket and quantity', async () => {
    // ARRANGE
    const mockTickets = [
      { id: 1, name: 'General Admission', price: 25.00, availability: 'AVAILABLE', maxPurchaseQuantity: 10 },
      { id: 2, name: 'VIP Pass', price: 75.00, availability: 'AVAILABLE', maxPurchaseQuantity: 5 }
    ];
    const user = userEvent.setup();

    // ACT - Part 1: Render the component and get our mock function back.
    const { mockOnPurchaseInitiate } = renderComponentWithProviders(mockTickets, true);

    // ACT - Part 2: Simulate the full user interaction.
    const ticketOption = screen.getByText(/general admission/i);
    await user.click(ticketOption);

    // The quantity input now exists. Let's find it.
    // `getByLabelText` is the best way to find form inputs. It's great for accessibility.
    const quantityInput = screen.getByLabelText(/qty:/i);
    
    // Simulate clearing the default value ('1') and typing a new one ('3').
    await user.clear(quantityInput);
    await user.type(quantityInput, '3');

    const purchaseButton = screen.getByRole('button', { name: /purchase ticket/i });
    await user.click(purchaseButton);

    // ASSERT
    // Now we check if our mock function was called, and what it was called with.
    
    // 1. Was the function called exactly one time?
    expect(mockOnPurchaseInitiate).toHaveBeenCalledTimes(1);
    
    // 2. Was it called with the correct arguments?
    //    - The first argument should be the FULL ticket object we clicked on.
    //    - The second argument should be the NUMBER we typed into the input.
    expect(mockOnPurchaseInitiate).toHaveBeenCalledWith(
      mockTickets[0], // This is the 'General Admission' ticket object
      3               // This is the quantity we typed
    );
  });


  it('should not allow selection of a sold out ticket', async () => {
  // ARRANGE
  const mockTickets = [
    { id: 1, name: 'General Admission', price: 25.00, availability: 'AVAILABLE', maxPurchaseQuantity: 10 },
    { id: 2, name: 'Early Bird', price: 20.00, availability: 'SOLD_OUT', maxPurchaseQuantity: 0 }
  ];
  const user = userEvent.setup();

  // ACT
  const { mockOnPurchaseInitiate } = renderComponentWithProviders(mockTickets, true);
  
  // Find the sold-out ticket element
  const soldOutTicketOption = screen.getByText(/early bird/i);
  
  // Simulate the user clicking on the disabled ticket
  await user.click(soldOutTicketOption);
  
  // ASSERT
  
  // 1. The purchase button should still be disabled.
  const purchaseButton = screen.getByRole('button', { name: /purchase ticket/i });
  expect(purchaseButton).toBeDisabled();

  // 2. The quantity input should NOT have appeared.
  const quantityInput = screen.queryByLabelText(/qty:/i);
  expect(quantityInput).not.toBeInTheDocument();
  
  // 3. (Optional but good) Let's verify the `onPurchaseInitiate` was not involved.
  // After clicking the purchase button (which we can't do because it's disabled),
  // the mock function should not have been called.
  expect(mockOnPurchaseInitiate).not.toHaveBeenCalled();
});

});