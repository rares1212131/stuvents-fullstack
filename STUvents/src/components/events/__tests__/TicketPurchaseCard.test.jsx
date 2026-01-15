import { it, expect, describe, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { AuthContext } from '../../../context/AuthContext';
import { TicketPurchaseCard } from '../TicketPurchaseCard';

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

  it('should render with the purchase button disabled if the user is logged in', () => {
    const mockTickets = [
      { id: 1, name: 'General Admission', price: 25.00, availability: 'AVAILABLE', maxPurchaseQuantity: 10 }
    ];
    renderComponentWithProviders(mockTickets, true);
    const purchaseButton = screen.getByRole('button', { name: /purchase ticket/i });
    expect(purchaseButton).toBeDisabled();
  });

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

  it('should call onPurchaseInitiate with the correct ticket and quantity', async () => {

    const mockTickets = [
      { id: 1, name: 'General Admission', price: 25.00, availability: 'AVAILABLE', maxPurchaseQuantity: 10 },
      { id: 2, name: 'VIP Pass', price: 75.00, availability: 'AVAILABLE', maxPurchaseQuantity: 5 }
    ];
    const user = userEvent.setup();

    const { mockOnPurchaseInitiate } = renderComponentWithProviders(mockTickets, true);

    const ticketOption = screen.getByText(/general admission/i);
    await user.click(ticketOption);

    const quantityInput = screen.getByLabelText(/qty:/i);
   
    await user.clear(quantityInput);
    await user.type(quantityInput, '3');

    const purchaseButton = screen.getByRole('button', { name: /purchase ticket/i });
    await user.click(purchaseButton);

    expect(mockOnPurchaseInitiate).toHaveBeenCalledTimes(1);
   
    expect(mockOnPurchaseInitiate).toHaveBeenCalledWith(
      mockTickets[0], 
      3               
    );
  });


  it('should not allow selection of a sold out ticket', async () => {
  const mockTickets = [
    { id: 1, name: 'General Admission', price: 25.00, availability: 'AVAILABLE', maxPurchaseQuantity: 10 },
    { id: 2, name: 'Early Bird', price: 20.00, availability: 'SOLD_OUT', maxPurchaseQuantity: 0 }
  ];
  const user = userEvent.setup();

  const { mockOnPurchaseInitiate } = renderComponentWithProviders(mockTickets, true);
  
  const soldOutTicketOption = screen.getByText(/early bird/i);
  
  await user.click(soldOutTicketOption);
  
  const purchaseButton = screen.getByRole('button', { name: /purchase ticket/i });
  expect(purchaseButton).toBeDisabled();

  const quantityInput = screen.queryByLabelText(/qty:/i);
  expect(quantityInput).not.toBeInTheDocument();

  expect(mockOnPurchaseInitiate).not.toHaveBeenCalled();
});

});