import React from 'react';
import ReactDOM from 'react-dom';
import { act, Simulate } from 'react-dom/test-utils';
import { MemoryRouter } from 'react-router-dom';
import App from './index';

jest.mock('../Cart', () => props => (
  <div>
    <div>cart-page</div>
    <div>cart-total-{props.cart.total}-{String(!!props.cart.error)}</div>
    <button className="cart-remove" onClick={() => props.removeItemFromCart({ id: 'sku-1', title: 'Cart Item' })}>remove</button>
    <button className="cart-refresh" onClick={props.fetchCart}>refresh</button>
  </div>
));
jest.mock('../ShowProduct', () => props => (
  <button className="showproduct-add" onClick={() => props.addItemToCart({ id: 'sku-3', title: 'Detail Item' })}>show-product-page</button>
));
jest.mock('../Products', () => props => (
  <div>
    <div>products-page-{props.category || props.sort || (props.match && props.match.params.category) || 'none'}</div>
    <button className="products-add" onClick={() => props.addItemToCart({ id: 'sku-2', title: 'List Item' })}>add-product</button>
  </div>
));
jest.mock('../Home', () => props => (
  <div>
    <div>home-page</div>
    <button className="home-add" onClick={() => props.addItemToCart({ id: { asin: 'sku-1' }, title: 'Home Item' })}>add-home</button>
  </div>
));
jest.mock('../Main/components', () => ({
  Navbar: props => <div>navbar-{props.cart.total}-{String(props.scrolled)}-{String(!!props.cart.error)}</div>,
  Footer: () => <div>footer</div>,
  Subscribe: () => <div>subscribe</div>,
}));

const flushPromises = () => new Promise(resolve => setTimeout(resolve, 0));
const flushMicrotasks = () => Promise.resolve();

describe('App', () => {
  let container;
  let originalRemoveEventListener;
  let originalSetTimeout;

  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);
    originalRemoveEventListener = window.removeEventListener;
    originalSetTimeout = global.setTimeout;
    window.removeEventListener = jest.fn();
    global.fetch = jest.fn(() => Promise.resolve({
      json: () => Promise.resolve({ book: '2', music: '1' })
    }));
  });

  afterEach(() => {
    ReactDOM.unmountComponentAtNode(container);
    document.body.removeChild(container);
    container = null;
    window.removeEventListener = originalRemoveEventListener;
    global.setTimeout = originalSetTimeout;
    jest.clearAllMocks();
  });

  it('fetches the cart on mount and renders the home route', async () => {
    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={["/"]}>
          <App />
        </MemoryRouter>,
        container
      );
      await flushMicrotasks();
      await flushMicrotasks();
    });

    expect(global.fetch).toHaveBeenCalledWith('/cart/get', expect.objectContaining({ method: 'POST' }));
    expect(container.textContent).toContain('home-page');
    expect(container.textContent).toContain('navbar-3-false-false');
    expect(container.textContent).toContain('subscribe');
    expect(container.textContent).toContain('footer');
  });

  it('renders the cart route when navigating to /cart', async () => {
    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={["/cart"]}>
          <App />
        </MemoryRouter>,
        container
      );
      await flushPromises();
    });

    expect(container.textContent).toContain('cart-page');
  });

  it('renders category-specific product routes', async () => {
    const cases = [
      ['/Music', 'products-page-Music'],
      ['/Books', 'products-page-Books'],
      ['/Beauty', 'products-page-Beauty'],
      ['/Electronics', 'products-page-Electronics']
    ];

    for (const [route, expectedText] of cases) {
      ReactDOM.unmountComponentAtNode(container);

      await act(async () => {
        ReactDOM.render(
          <MemoryRouter initialEntries={[route]}>
            <App />
          </MemoryRouter>,
          container
        );
        await flushPromises();
      });

      expect(container.textContent).toContain(expectedText);
    }
  });

  it('renders the dynamic category, sort, and item routes', async () => {
    const cases = [
      ['/Kitchen', 'products-page-Kitchen'],
      ['/sort/num_reviews', 'products-page-num_reviews'],
      ['/item/sku-3', 'show-product-page']
    ];

    for (const [route, expectedText] of cases) {
      ReactDOM.unmountComponentAtNode(container);

      await act(async () => {
        ReactDOM.render(
          <MemoryRouter initialEntries={[route]}>
            <App />
          </MemoryRouter>,
          container
        );
        await flushPromises();
      });

      expect(container.textContent).toContain(expectedText);
    }
  });

  it('adds an item to the cart from the home route and updates the navbar total', async () => {
    global.fetch = jest.fn()
      .mockImplementationOnce(() => Promise.resolve({ json: () => Promise.resolve({}) }))
      .mockImplementationOnce(() => Promise.resolve({ json: () => Promise.resolve({ 'sku-1': 2 }) }));

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={["/"]}>
          <App />
        </MemoryRouter>,
        container
      );
      await flushPromises();
    });

    await act(async () => {
      Simulate.click(container.querySelector('.home-add'));
      await flushPromises();
    });

    expect(global.fetch).toHaveBeenCalledWith('/cart/add?asin=sku-1', expect.objectContaining({ method: 'POST' }));
    expect(container.textContent).toContain('navbar-2-false-false');
  });

  it('removes an item from the cart route and updates the navbar total', async () => {
    global.fetch = jest.fn()
      .mockImplementationOnce(() => Promise.resolve({ json: () => Promise.resolve({ 'sku-1': '1' }) }))
      .mockImplementationOnce(() => Promise.resolve({ json: () => Promise.resolve({}) }));

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={["/cart"]}>
          <App />
        </MemoryRouter>,
        container
      );
      await flushPromises();
    });

    expect(container.textContent).toContain('cart-total-1-false');

    await act(async () => {
      Simulate.click(container.querySelector('.cart-remove'));
      await flushPromises();
    });

    expect(global.fetch).toHaveBeenCalledWith('/cart/remove/?asin=sku-1', expect.objectContaining({ method: 'POST' }));
    expect(container.textContent).toContain('navbar-0-false-false');
  });

  it('sets and clears the cart error flag when add to cart fails', async () => {
    const app = new App({});
    let timeoutCallback;
    const logSpy = jest.spyOn(console, 'log').mockImplementation(() => {});
    const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});

    global.setTimeout = jest.fn(callback => {
      timeoutCallback = callback;
      return 1;
    });
    global.fetch = jest.fn(() => Promise.reject(new Error('network')));

    app.setState = jest.fn(update => {
      const nextState = typeof update === 'function' ? update(app.state) : update;
      app.state = { ...app.state, ...nextState };
    });

    app.addItemToCart({ id: { asin: 'sku-1' }, title: 'Home Item' });
    await flushMicrotasks();
    await flushMicrotasks();
    await flushMicrotasks();
    await flushMicrotasks();

    expect(app.setState).toHaveBeenCalled();
    expect(app.state.cart.error).toBe(true);

    timeoutCallback();

    expect(app.state.cart.error).toBe(false);
    logSpy.mockRestore();
    warnSpy.mockRestore();
  });

  it('fetchCart calculates the total and clears cart errors', async () => {
    const app = new App({});
    global.fetch = jest.fn(() => Promise.resolve({
      json: () => Promise.resolve({ books: '2', music: '1.5' })
    }));

    app.setState = jest.fn(update => {
      const nextState = typeof update === 'function' ? update(app.state) : update;
      app.state = { ...app.state, ...nextState };
    });

    app.fetchCart();
    await flushMicrotasks();
    await flushMicrotasks();
    await flushMicrotasks();
    await flushMicrotasks();

    expect(app.state.cart.data).toEqual({ books: '2', music: '1.5' });
    expect(app.state.cart.total).toBe(3.5);
    expect(app.state.cart.error).toBe(false);
  });

  it('updates the scrolled state in response to the registered scroll handler', () => {
    const app = new App({});
    let scrollHandler;
    const addEventListenerSpy = jest.spyOn(window, 'addEventListener').mockImplementation((eventName, handler) => {
      if (eventName === 'scroll') {
        scrollHandler = handler;
      }
    });

    app.setState = jest.fn(update => {
      const nextState = typeof update === 'function' ? update(app.state) : update;
      app.state = { ...app.state, ...nextState };
    });

    window.pageYOffset = 100;
    app.componentWillMount();
    scrollHandler();
    expect(app.state.scrolled).toBe(true);

    window.pageYOffset = 0;
    scrollHandler();
    expect(app.state.scrolled).toBe(false);

    addEventListenerSpy.mockRestore();
  });

  it('sets and clears the cart error flag when remove from cart fails', async () => {
    const app = new App({});
    let timeoutCallback;
    const logSpy = jest.spyOn(console, 'log').mockImplementation(() => {});
    const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});

    global.setTimeout = jest.fn(callback => {
      timeoutCallback = callback;
      return 1;
    });
    global.fetch = jest.fn(() => Promise.reject(new Error('network')));

    app.setState = jest.fn(update => {
      const nextState = typeof update === 'function' ? update(app.state) : update;
      app.state = { ...app.state, ...nextState };
    });

    app.removeItemFromCart({ id: 'sku-1', title: 'Cart Item' });
    await flushMicrotasks();
    await flushMicrotasks();
    await flushMicrotasks();
    await flushMicrotasks();

    expect(app.state.cart.error).toBe(true);

    timeoutCallback();

    expect(app.state.cart.error).toBe(false);
    logSpy.mockRestore();
    warnSpy.mockRestore();
  });

  it('ignores add and remove cart requests when no product is provided', () => {
    const app = new App({});
    global.fetch = jest.fn();

    app.addItemToCart();
    app.removeItemFromCart();

    expect(global.fetch).not.toHaveBeenCalled();
  });

  it('totals only own enumerable cart values', () => {
    const app = new App({});
    const data = Object.create({ inherited: '100' });
    data.books = '2';
    data.music = '1.5';

    expect(app.totalReducer(data)).toBe(3.5);
  });
});