import React from 'react';
import ReactDOM from 'react-dom';
import { act, Simulate } from 'react-dom/test-utils';
import { MemoryRouter } from 'react-router-dom';
import Cart from './index';

jest.mock('../../components/common', () => ({
  Button: props => (
    <button disabled={props.disabled} onClick={props.onClick} className={props.className}>
      {props.children}
    </button>
  )
}));

const flushPromises = () => new Promise(resolve => setTimeout(resolve, 0));

describe('Cart', () => {
  let container;
  const cartProduct = {
    id: 'sku-1',
    title: 'Coffee Beans',
    price: 12.5,
    imUrl: 'https://example.com/coffee.png'
  };

  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);
    global.fetch = jest.fn()
      .mockImplementationOnce(() => Promise.resolve({
        json: () => Promise.resolve(cartProduct)
      }))
      .mockImplementationOnce(() => Promise.resolve({
        json: () => Promise.resolve({ orderNumber: '42', orderDetails: 'Order packed and ready.' })
      }));
  });

  afterEach(() => {
    ReactDOM.unmountComponentAtNode(container);
    document.body.removeChild(container);
    container = null;
    jest.clearAllMocks();
  });

  it('renders fetched cart items and calculates the total', async () => {
    const removeItemFromCart = jest.fn();

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter>
          <Cart
            cart={{ data: { 'sku-1': 2 }, total: 2 }}
            currentUser={{ userId: '42', email: 'shopper@example.com' }}
            fetchCart={jest.fn()}
            removeItemFromCart={removeItemFromCart}
          />
        </MemoryRouter>,
        container
      );
      await flushPromises();
    });

    expect(global.fetch).toHaveBeenCalledWith('/products/details?asin=sku-1');
    expect(container.textContent).toContain('Items in cart');
    expect(container.textContent).toContain('Coffee Beans');
    expect(container.textContent).toContain('$12.50');
    expect(container.textContent).toContain('Total:');
    expect(container.textContent).toContain('$25.00');

    const removeButton = Array.from(container.querySelectorAll('button')).find(button => button.textContent === 'Remove');
    Simulate.click(removeButton);

    expect(removeItemFromCart).toHaveBeenCalledWith(cartProduct);
  });

  it('submits checkout and refreshes the cart', async () => {
    const fetchCart = jest.fn();

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter>
          <Cart
            cart={{ data: { 'sku-1': 2 }, total: 2 }}
            currentUser={{ userId: '42', email: 'shopper@example.com' }}
            fetchCart={fetchCart}
            removeItemFromCart={jest.fn()}
          />
        </MemoryRouter>,
        container
      );
      await flushPromises();
    });

    const checkoutButton = Array.from(container.querySelectorAll('button')).find(button => button.textContent === 'Checkout');

    await act(async () => {
      Simulate.click(checkoutButton);
      await flushPromises();
    });

    expect(global.fetch).toHaveBeenLastCalledWith('/cart/checkout', expect.objectContaining({ method: 'POST' }));
    expect(fetchCart).toHaveBeenCalled();
    expect(container.textContent).toContain('Thank you!');
    expect(container.textContent).toContain('Order packed and ready.');
  });

  it('shows an empty-cart message and does not attempt checkout when total is zero', async () => {
    global.fetch = jest.fn();

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter>
          <Cart
            cart={{ data: {}, total: 0 }}
            currentUser={{ userId: '42', email: 'shopper@example.com' }}
            fetchCart={jest.fn()}
            removeItemFromCart={jest.fn()}
          />
        </MemoryRouter>,
        container
      );
      await flushPromises();
    });

    expect(container.textContent).toContain('Cart is empty');
    expect(container.querySelectorAll('button')).toHaveLength(0);
    expect(global.fetch).not.toHaveBeenCalled();
  });

  it('does not call checkout when submitCheckout runs with an empty cart', () => {
    const cart = new Cart({ cart: { data: {}, total: 0 }, fetchCart: jest.fn(), removeItemFromCart: jest.fn() });
    global.fetch = jest.fn();

    cart.submitCheckout();

    expect(global.fetch).not.toHaveBeenCalled();
  });
});