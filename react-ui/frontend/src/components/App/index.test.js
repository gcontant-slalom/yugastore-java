import React from 'react';
import ReactDOM from 'react-dom';
import { act, Simulate } from 'react-dom/test-utils';
import { MemoryRouter, Router } from 'react-router-dom';
import { createMemoryHistory } from 'history';
import WrappedApp, { App } from './index';

jest.mock('../Cart', () => props => (
  <div>
    <div>cart-page</div>
    <div>cart-total-{props.cart.total}-{String(!!props.cart.error)}</div>
    <button className="cart-remove" onClick={() => props.removeItemFromCart({ id: 'sku-1', title: 'Cart Item' })}>remove</button>
    <button className="cart-refresh" onClick={props.fetchCart}>refresh</button>
  </div>
));
jest.mock('../ShowProduct', () => props => (
  <button className="showproduct-add" onClick={() => props.addItemToCart({ id: 'sku-3', title: 'Detail Item' })}>show-product-page-{props.tenantKey || 'demo'}</button>
));
jest.mock('../Products', () => props => (
  <div>
    <div>products-page-{props.category || props.sort || (props.match && props.match.params.category) || 'none'}</div>
    <div>products-tenant-{props.tenantKey || 'demo'}</div>
    <button className="products-add" onClick={() => props.addItemToCart({ id: 'sku-2', title: 'List Item' })}>add-product</button>
  </div>
));
jest.mock('../Home', () => props => (
  <div>
    <div>home-page-{props.tenantContext ? props.tenantContext.tenantKey : 'demo'}</div>
    <button className="home-add" onClick={() => props.addItemToCart({ id: { asin: 'sku-1' }, title: 'Home Item' })}>add-home</button>
  </div>
));
jest.mock('../Auth', () => props => (
  <div>
    <div>auth-page-{props.mode}</div>
    <button className={`auth-submit-${props.mode}`} onClick={() => props.mode === 'register'
      ? props.onRegister({ email: 'merchant@example.com', password: 'password123', passwordConfirm: 'password123' })
      : props.onLogin({ email: 'merchant@example.com', password: 'password123' })}>submit-auth</button>
  </div>
));
jest.mock('../Main/components', () => ({
  Navbar: props => <div>navbar-{props.cart.total}-{String(props.scrolled)}-{String(!!props.cart.error)}-{props.currentUser ? props.currentUser.email : 'guest'}</div>,
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
    global.fetch = jest.fn()
      .mockImplementationOnce(() => Promise.resolve({ ok: false, status: 401, text: () => Promise.resolve('') }))
      .mockImplementation(() => Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(JSON.stringify({ book: '2', music: '1' })) }));
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
          <WrappedApp />
        </MemoryRouter>,
        container
      );
      await flushMicrotasks();
      await flushMicrotasks();
    });

    expect(global.fetch).toHaveBeenCalledWith('/auth/current-user', expect.objectContaining({ method: 'GET' }));
    expect(container.textContent).toContain('home-page');
    expect(container.textContent).toContain('navbar-0-false-false-guest');
    expect(container.textContent).toContain('subscribe');
    expect(container.textContent).toContain('footer');
  });

  it('rehydrates merchant context on bootstrap when current user is authenticated', async () => {
    const app = new App({});
    global.fetch = jest.fn()
      .mockImplementationOnce(() => Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(JSON.stringify({ userId: '42', email: 'merchant@example.com' })) }))
      .mockImplementationOnce(() => Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(JSON.stringify({})) }))
      .mockImplementationOnce(() => Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(JSON.stringify([{ tenantId: '8', tenantKey: 'northwind-books', companyName: 'Northwind Books' }, { tenantId: '9', tenantKey: 'northwind-music', companyName: 'Northwind Music' }])) }));

    app.setState = jest.fn(update => {
      const nextState = typeof update === 'function' ? update(app.state) : update;
      app.state = { ...app.state, ...nextState };
    });

    await app.fetchCurrentUser();
    await flushMicrotasks();

    expect(app.state.merchantContexts).toHaveLength(2);
    expect(app.state.merchantContext).toEqual({ tenantId: '9', tenantKey: 'northwind-music', companyName: 'Northwind Music' });
    expect(app.state.merchantSignupResult).toBeNull();
  });

  it('rehydrates merchant context after login', async () => {
    const app = new App({});
    global.fetch = jest.fn()
      .mockImplementationOnce(() => Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(JSON.stringify({ userId: '42', email: 'merchant@example.com' })) }))
      .mockImplementationOnce(() => Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(JSON.stringify({})) }))
      .mockImplementationOnce(() => Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(JSON.stringify([{ tenantId: '8', tenantKey: 'northwind-books', companyName: 'Northwind Books' }, { tenantId: '9', tenantKey: 'northwind-music', companyName: 'Northwind Music' }])) }));

    app.setState = jest.fn(update => {
      const nextState = typeof update === 'function' ? update(app.state) : update;
      app.state = { ...app.state, ...nextState };
    });

    await app.login({ email: 'merchant@example.com', password: 'password123' });
    await flushMicrotasks();

    expect(app.state.currentUser.email).toBe('merchant@example.com');
    expect(app.state.merchantContexts).toHaveLength(2);
    expect(app.state.merchantContext.tenantKey).toBe('northwind-music');
  });

  it('clears merchant context when no tenant is linked to the user', async () => {
    const app = new App({});
    app.state.currentUser = { userId: '42', email: 'merchant@example.com' };
    app.state.merchantContexts = [{ tenantKey: 'old-tenant' }];
    app.state.merchantContext = { tenantKey: 'old-tenant' };
    global.fetch = jest.fn(() => Promise.resolve({ ok: false, status: 404, text: () => Promise.resolve(JSON.stringify({ message: 'No merchant tenant is linked to this account.' })) }));

    app.setState = jest.fn(update => {
      const nextState = typeof update === 'function' ? update(app.state) : update;
      app.state = { ...app.state, ...nextState };
    });

    await app.fetchMerchantContexts();
    await flushMicrotasks();

    expect(app.state.merchantContexts).toEqual([]);
    expect(app.state.merchantContext).toBeNull();
  });

  it('renders the cart route when navigating to /cart', async () => {
    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={["/cart"]}>
          <WrappedApp />
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
            <WrappedApp />
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
            <WrappedApp />
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
      .mockImplementationOnce(() => Promise.resolve({ ok: false, status: 401, text: () => Promise.resolve('') }))
      .mockImplementationOnce(() => Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(JSON.stringify({ 'sku-1': 2 })) }));

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={["/"]}>
          <WrappedApp />
        </MemoryRouter>,
        container
      );
      await flushPromises();
    });

    await act(async () => {
      Simulate.click(container.querySelector('.home-add'));
      await flushPromises();
    });

    expect(container.textContent).toContain('navbar-0-false-false-guest');
  });

  it('removes an item from the cart route and updates the navbar total', async () => {
    global.fetch = jest.fn()
      .mockImplementationOnce(() => Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(JSON.stringify({ userId: '42', email: 'merchant@example.com' })) }))
      .mockImplementationOnce(() => Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(JSON.stringify({ 'sku-1': '1' })) }))
      .mockImplementationOnce(() => Promise.resolve({ ok: false, status: 404, text: () => Promise.resolve(JSON.stringify({ message: 'No merchant tenant is linked to this account.' })) }))
      .mockImplementationOnce(() => Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(JSON.stringify({})) }));

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={["/cart"]}>
          <WrappedApp />
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
    expect(container.textContent).toContain('navbar-0-false-false-merchant@example.com');
  });

  it('stores a newly created merchant tenant in the visible tenant list', async () => {
    const app = new App({});
    app.state.currentUser = { userId: '42', email: 'merchant@example.com' };
    app.state.merchantContexts = [{ tenantId: '8', tenantKey: 'northwind-books', companyName: 'Northwind Books' }];
    global.fetch = jest.fn(() => Promise.resolve({
      ok: true,
      status: 200,
      text: () => Promise.resolve(JSON.stringify({ tenantId: '9', tenantKey: 'northwind-music', companyName: 'Northwind Music' }))
    }));

    app.setState = jest.fn(update => {
      const nextState = typeof update === 'function' ? update(app.state) : update;
      app.state = { ...app.state, ...nextState };
    });

    await app.createMerchantSignup({ companyName: 'Northwind Music', tenantKey: 'northwind-music' });
    await flushMicrotasks();

    expect(app.state.merchantContexts).toHaveLength(2);
    expect(app.state.merchantContext.tenantKey).toBe('northwind-music');
    expect(app.state.merchantSignupResult.tenantKey).toBe('northwind-music');
  });

  it('clears transient merchant signup feedback when the onboarding route is left', () => {
    const app = new App({});
    app.state.merchantSignupMessage = 'Created';
    app.state.merchantSignupResult = { tenantKey: 'northwind-music' };

    app.setState = jest.fn(update => {
      const nextState = typeof update === 'function' ? update(app.state) : update;
      app.state = { ...app.state, ...nextState };
    });

    app.clearMerchantSignupFeedback();

    expect(app.state.merchantSignupMessage).toBe('');
    expect(app.state.merchantSignupResult).toBeNull();
  });

  it('renders the tenant-scoped merchant signup route', async () => {
    global.fetch = jest.fn(() => Promise.resolve({ ok: false, status: 401, text: () => Promise.resolve('') }));

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={['/northwind-books/signup']}>
          <WrappedApp />
        </MemoryRouter>,
        container
      );
      await flushPromises();
    });

    expect(container.textContent).toContain('Create your merchant tenant');
    expect(container.querySelector('input[name="tenantKey"]').value).toBe('northwind-books');
  });

  it('does not restore the merchant-created confirmation after a fresh onboarding page load', async () => {
    global.fetch = jest.fn()
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify({ userId: '42', email: 'merchant@example.com' }))
      }))
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify({ tenantId: '9', tenantKey: 'northwind-books', companyName: 'Northwind Books' }))
      }))
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify({}))
      }))
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify([]))
      }))
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify({ tenantId: '9', tenantKey: 'northwind-books', companyName: 'Northwind Books' }))
      }));

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={['/northwind-books/signup']}>
          <WrappedApp />
        </MemoryRouter>,
        container
      );
      await flushPromises();
      await flushPromises();
    });

    await act(async () => {
      Simulate.change(container.querySelector('input[name="companyName"]'), { target: { name: 'companyName', value: 'Northwind Books' } });
      Simulate.change(container.querySelector('input[name="tenantKey"]'), { target: { name: 'tenantKey', value: 'northwind-books' } });
      await flushPromises();
    });

    await act(async () => {
      Simulate.submit(container.querySelector('.auth-form'));
      await flushPromises();
      await flushPromises();
    });

    expect(container.textContent).toContain('Merchant tenant created');
    expect(container.textContent).toContain('Northwind Books is now linked to your account.');

    ReactDOM.unmountComponentAtNode(container);

    global.fetch = jest.fn()
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify({ userId: '42', email: 'merchant@example.com' }))
      }))
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify({ tenantId: '9', tenantKey: 'northwind-books', companyName: 'Northwind Books' }))
      }))
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify({}))
      }))
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify([{ tenantId: '9', tenantKey: 'northwind-books', companyName: 'Northwind Books' }]))
      }));

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={['/northwind-books/signup']}>
          <WrappedApp />
        </MemoryRouter>,
        container
      );
      await flushPromises();
      await flushPromises();
    });

    expect(container.textContent).toContain('Create your merchant tenant');
    expect(container.textContent).toContain('Your tenants');
    expect(container.textContent).toContain('Northwind Books');
    expect(container.textContent).not.toContain('Merchant tenant created');
  });

  it('clears the merchant-created confirmation after leaving and returning to onboarding', async () => {
    const history = createMemoryHistory({ initialEntries: ['/northwind-books/signup'] });

    global.fetch = jest.fn()
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify({ userId: '42', email: 'merchant@example.com' }))
      }))
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify({ tenantId: '9', tenantKey: 'northwind-books', companyName: 'Northwind Books' }))
      }))
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify({}))
      }))
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify([]))
      }))
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify({ tenantId: '9', tenantKey: 'northwind-books', companyName: 'Northwind Books' }))
      }))
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify({ tenantId: '9', tenantKey: 'northwind-books', companyName: 'Northwind Books' }))
      }));

    await act(async () => {
      ReactDOM.render(
        <Router history={history}>
          <WrappedApp />
        </Router>,
        container
      );
      await flushPromises();
      await flushPromises();
    });

    await act(async () => {
      Simulate.change(container.querySelector('input[name="companyName"]'), { target: { name: 'companyName', value: 'Northwind Books' } });
      Simulate.change(container.querySelector('input[name="tenantKey"]'), { target: { name: 'tenantKey', value: 'northwind-books' } });
      await flushPromises();
    });

    await act(async () => {
      Simulate.submit(container.querySelector('.auth-form'));
      await flushPromises();
      await flushPromises();
    });

    expect(container.textContent).toContain('Merchant tenant created');

    await act(async () => {
      history.push('/Books');
      await flushPromises();
    });

    expect(container.textContent).toContain('products-page-Books');

    await act(async () => {
      history.push('/northwind-books/signup');
      await flushPromises();
      await flushPromises();
    });

    expect(container.textContent).toContain('Create your merchant tenant');
    expect(container.textContent).toContain('Your tenants');
    expect(container.textContent).toContain('Northwind Books');
    expect(container.textContent).not.toContain('Merchant tenant created');
  });

  it('resolves tenant storefront context from the slug route', async () => {
    global.fetch = jest.fn()
      .mockImplementationOnce(() => Promise.resolve({ ok: false, status: 401, text: () => Promise.resolve('') }))
      .mockImplementationOnce(() => Promise.resolve({
        ok: true,
        status: 200,
        text: () => Promise.resolve(JSON.stringify({ tenantId: '8', tenantKey: 'northwind-books', companyName: 'Northwind Books' }))
      }));

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={['/northwind-books']}>
          <WrappedApp />
        </MemoryRouter>,
        container
      );
      await flushPromises();
      await flushPromises();
    });

    expect(global.fetch).toHaveBeenCalledWith('/api/v1/merchant-context/tenant/northwind-books', expect.objectContaining({ method: 'GET' }));
    expect(container.textContent).toContain('home-page-northwind-books');
  });

  it('shows an invalid-tenant outcome instead of falling back to the demo storefront', async () => {
    global.fetch = jest.fn()
      .mockImplementationOnce(() => Promise.resolve({ ok: false, status: 401, text: () => Promise.resolve('') }))
      .mockImplementationOnce(() => Promise.resolve({
        ok: false,
        status: 404,
        text: () => Promise.resolve(JSON.stringify({ message: 'No merchant tenant matches that storefront path.' }))
      }));

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={['/unknown-store']}>
          <WrappedApp />
        </MemoryRouter>,
        container
      );
      await flushPromises();
      await flushPromises();
    });

    expect(container.textContent).toContain('Unknown tenant storefront');
    expect(container.textContent).not.toContain('home-page-demo');
  });

  it('renders tenant-scoped category and item routes with the resolved tenant context', async () => {
    const cases = [
      ['/northwind-books/Books', 'products-tenant-northwind-books'],
      ['/northwind-books/item/sku-3', 'show-product-page-northwind-books']
    ];

    for (const [route, expectedText] of cases) {
      global.fetch = jest.fn()
        .mockImplementationOnce(() => Promise.resolve({ ok: false, status: 401, text: () => Promise.resolve('') }))
        .mockImplementationOnce(() => Promise.resolve({
          ok: true,
          status: 200,
          text: () => Promise.resolve(JSON.stringify({ tenantId: '8', tenantKey: 'northwind-books', companyName: 'Northwind Books' }))
        }));

      ReactDOM.unmountComponentAtNode(container);

      await act(async () => {
        ReactDOM.render(
          <MemoryRouter initialEntries={[route]}>
            <WrappedApp />
          </MemoryRouter>,
          container
        );
        await flushPromises();
        await flushPromises();
      });

      expect(container.textContent).toContain(expectedText);
    }
  });

  it('sets and clears the cart error flag when add to cart fails', async () => {
    const app = new App({});
    app.state.currentUser = { userId: '42', email: 'merchant@example.com' };
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
    app.state.currentUser = { userId: '42', email: 'merchant@example.com' };
    global.fetch = jest.fn(() => Promise.resolve({
      ok: true,
      status: 200,
      text: () => Promise.resolve(JSON.stringify({ books: '2', music: '1.5' }))
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

  it('renders login and register routes', async () => {
    const cases = [
      ['/login', 'auth-page-login'],
      ['/register', 'auth-page-register']
    ];

    for (const [route, expected] of cases) {
      global.fetch = jest.fn(() => Promise.resolve({ ok: false, status: 401, text: () => Promise.resolve('') }));
      ReactDOM.unmountComponentAtNode(container);

      await act(async () => {
        ReactDOM.render(
          <MemoryRouter initialEntries={[route]}>
            <WrappedApp />
          </MemoryRouter>,
          container
        );
        await flushPromises();
      });

      expect(container.textContent).toContain(expected);
    }
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
    app.state.currentUser = { userId: '42', email: 'merchant@example.com' };
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

  it('stores active tenant context when adding a tenant-scoped product to the cart', async () => {
    const app = new App({});
    app.state.currentUser = { userId: '42', email: 'merchant@example.com' };
    app.state.activeTenantContext = { tenantKey: 'northwind-books', companyName: 'Northwind Books' };
    global.fetch = jest.fn(() => Promise.resolve({
      ok: true,
      status: 200,
      text: () => Promise.resolve(JSON.stringify({ 'sku-1': 1 }))
    }));

    app.setState = jest.fn(update => {
      const nextState = typeof update === 'function' ? update(app.state) : update;
      app.state = { ...app.state, ...nextState };
    });

    app.addItemToCart({ id: { asin: 'sku-1' }, title: 'Home Item' });
    await flushMicrotasks();
    await flushMicrotasks();
    await flushMicrotasks();
    await flushMicrotasks();

    expect(app.state.cartTenantContext).toEqual({ tenantKey: 'northwind-books', companyName: 'Northwind Books' });
    expect(global.fetch).toHaveBeenCalledWith('/cart/add?asin=sku-1', expect.objectContaining({
      method: 'POST',
      headers: expect.objectContaining({
        'X-Tenant-Key': 'northwind-books'
      })
    }));
  });

  it('clears cart tenant context when fetchCart returns an empty cart', async () => {
    const app = new App({});
    app.state.currentUser = { userId: '42', email: 'merchant@example.com' };
    app.state.cartTenantContext = { tenantKey: 'northwind-books', companyName: 'Northwind Books' };
    global.fetch = jest.fn(() => Promise.resolve({
      ok: true,
      status: 200,
      text: () => Promise.resolve(JSON.stringify({}))
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

    expect(app.state.cartTenantContext).toBeNull();
  });

  it('totals only own enumerable cart values', () => {
    const app = new App({});
    const data = Object.create({ inherited: '100' });
    data.books = '2';
    data.music = '1.5';

    expect(app.totalReducer(data)).toBe(3.5);
  });
});