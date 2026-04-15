import React from 'react';
import ReactDOM from 'react-dom';
import { act, Simulate } from 'react-dom/test-utils';
import { MemoryRouter, Route } from 'react-router-dom';
import ShowProduct from './index';

jest.mock('react-materialize', () => ({
  Icon: props => <span className={props.className}>{props.children}</span>
}));

jest.mock('react-bootstrap', () => ({
  Row: props => <div>{props.children}</div>,
  Col: props => <div>{props.children}</div>
}));

jest.mock('../../components/common', () => ({
  Button: props => (
    <button disabled={props.disabled} onClick={props.onClick} className={props.className}>
      {props.children}
    </button>
  )
}));

const flushPromises = () => new Promise(resolve => setTimeout(resolve, 0));

describe('ShowProduct', () => {
  let container;
  const mainProduct = {
    id: 'sku-1',
    title: 'Primary Product',
    price: 24.5,
    avg_stars: 4.2,
    num_stars: 4.2,
    num_reviews: 10,
    description: 'Long form description',
    imUrl: 'https://example.com/primary.png',
    also_bought: ['sku-2', 'sku-3']
  };
  const relatedOne = {
    id: 'sku-2',
    title: 'Related One',
    price: 11,
    avg_stars: 4,
    num_stars: 4,
    num_reviews: 5,
    imUrl: 'https://example.com/related-1.png'
  };
  const relatedTwo = {
    id: 'sku-3',
    title: 'Related Two',
    price: 15,
    avg_stars: 4.8,
    num_stars: 4.8,
    num_reviews: 3,
    imUrl: 'https://example.com/related-2.png'
  };

  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);
    global.fetch = jest.fn(url => {
      if (url === '/products/details?asin=sku-1') {
        return Promise.resolve({ json: () => Promise.resolve(mainProduct) });
      }
      if (url === '/products/details?asin=sku-2') {
        return Promise.resolve({ json: () => Promise.resolve(relatedOne) });
      }
      if (url === '/products/details?asin=sku-3') {
        return Promise.resolve({ json: () => Promise.resolve(relatedTwo) });
      }
      return Promise.reject(new Error('Unexpected fetch: ' + url));
    });
  });

  afterEach(() => {
    ReactDOM.unmountComponentAtNode(container);
    document.body.removeChild(container);
    container = null;
    jest.clearAllMocks();
  });

  it('loads product details, related products, and supports add to cart', async () => {
    const addItemToCart = jest.fn();

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={['/item/sku-1']}>
          <Route path="/item/:asin" render={props => <ShowProduct {...props} addItemToCart={addItemToCart} />} />
        </MemoryRouter>,
        container
      );
      await flushPromises();
      await flushPromises();
    });

    expect(global.fetch).toHaveBeenCalledWith('/products/details?asin=sku-1');
    expect(global.fetch).toHaveBeenCalledWith('/products/details?asin=sku-2', expect.objectContaining({ headers: expect.any(Object) }));
    expect(global.fetch).toHaveBeenCalledWith('/products/details?asin=sku-3', expect.objectContaining({ headers: expect.any(Object) }));
    expect(container.textContent).toContain('Primary Product');
    expect(container.textContent).toContain('Long form description');
    expect(container.textContent).toContain('Also bought');
    expect(container.textContent).toContain('Related One');
    expect(container.textContent).toContain('Related Two');

    const buttons = Array.from(container.querySelectorAll('button'));
    Simulate.click(buttons[0]);
    Simulate.click(buttons[1]);

    expect(addItemToCart).toHaveBeenCalledWith(mainProduct);
    expect(addItemToCart).toHaveBeenCalledWith(relatedOne);
  });

  it('renders only the main product when there are no related items', async () => {
    const standaloneProduct = {
      id: 'sku-9',
      title: 'Standalone Product',
      price: 20,
      avg_stars: 2,
      num_stars: 2,
      num_reviews: 1,
      description: 'Single product',
      imUrl: 'https://example.com/standalone.png'
    };

    global.fetch = jest.fn(url => {
      if (url === '/products/details?asin=sku-9') {
        return Promise.resolve({ json: () => Promise.resolve(standaloneProduct) });
      }
      return Promise.reject(new Error('Unexpected fetch: ' + url));
    });

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={['/item/sku-9']}>
          <Route path="/item/:asin" render={props => <ShowProduct {...props} addItemToCart={jest.fn()} />} />
        </MemoryRouter>,
        container
      );
      await flushPromises();
      await flushPromises();
    });

    expect(container.textContent).toContain('Standalone Product');
    expect(container.textContent).not.toContain('Also bought');
  });

  it('ignores related product responses that do not contain an id', async () => {
    const instance = new ShowProduct({ match: { params: { asin: 'sku-1' } }, addItemToCart: jest.fn() });
    instance.state = { product_id: 'sku-1', product: mainProduct, productAlsoBought: [] };
    instance.setState = jest.fn(update => {
      const nextState = typeof update === 'function' ? update(instance.state) : update;
      instance.state = { ...instance.state, ...nextState };
    });
    global.fetch = jest.fn(url => {
      if (url === '/products/details?asin=missing') {
        return Promise.resolve({ json: () => Promise.resolve({}) });
      }
      return Promise.reject(new Error('Unexpected fetch: ' + url));
    });

    instance.fetchProductAlsoBought(['missing']);
    await flushPromises();
    await flushPromises();

    expect(instance.state.productAlsoBought).toEqual([]);
  });

  it('loads product details from the actual asin route param', async () => {
    const addItemToCart = jest.fn();

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={['/item/sku-1']}>
          <Route path="/item/:asin" render={props => <ShowProduct {...props} addItemToCart={addItemToCart} />} />
        </MemoryRouter>,
        container
      );
      await flushPromises();
      await flushPromises();
    });

    expect(container.textContent).toContain('Primary Product');
    expect(global.fetch).toHaveBeenCalledWith('/products/details?asin=sku-1');
  });

  it('uses tenant-scoped detail requests and links on tenant storefront routes', async () => {
    global.fetch = jest.fn(url => {
      if (url === '/tenant/northwind-books/products/details?asin=sku-1') {
        return Promise.resolve({ json: () => Promise.resolve(mainProduct) });
      }
      if (url === '/tenant/northwind-books/products/details?asin=sku-2') {
        return Promise.resolve({ json: () => Promise.resolve(relatedOne) });
      }
      if (url === '/tenant/northwind-books/products/details?asin=sku-3') {
        return Promise.resolve({ json: () => Promise.resolve(relatedTwo) });
      }
      return Promise.reject(new Error('Unexpected fetch: ' + url));
    });

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter initialEntries={['/northwind-books/item/sku-1']}>
          <Route path="/:tenantSlug/item/:asin" render={props => <ShowProduct {...props} addItemToCart={jest.fn()} />} />
        </MemoryRouter>,
        container
      );
      await flushPromises();
      await flushPromises();
    });

    expect(global.fetch).toHaveBeenCalledWith('/tenant/northwind-books/products/details?asin=sku-1');
    expect(container.querySelectorAll('a')[0].getAttribute('href')).toBe('/northwind-books/item/sku-2');
  });
});