import React from 'react';
import ReactDOM from 'react-dom';
import { act, Simulate } from 'react-dom/test-utils';
import { MemoryRouter } from 'react-router-dom';
import Products from './index';

jest.mock('react-materialize', () => ({
  Icon: props => <span className={props.className}>{props.children}</span>
}));

jest.mock('react-bootstrap', () => ({
  Row: props => <div className={props.className}>{props.children}</div>,
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

const createProducts = (count, startIndex, options) => (
  Array.from({ length: count }, (_, index) => ({
    id: { asin: 'asin-' + (startIndex + index) },
    title: 'Product ' + (startIndex + index),
    price: 10 + index,
    avg_stars: 4,
    num_stars: 4,
    num_reviews: 10 + index,
    num_buys: 100 - index,
    num_views: 200 - index,
    imUrl: 'https://example.com/product-' + (startIndex + index) + '.png',
    ...options
  }))
);

describe('Products', () => {
  let container;
  const product = {
    id: { asin: 'asin-1' },
    title: 'Test Product',
    price: 19.5,
    avg_stars: 4.5,
    num_stars: 4.5,
    num_reviews: 12,
    imUrl: 'https://example.com/product.png'
  };

  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);
    global.fetch = jest.fn(() => Promise.resolve({
      json: () => Promise.resolve([product])
    }));
  });

  afterEach(() => {
    ReactDOM.unmountComponentAtNode(container);
    document.body.removeChild(container);
    container = null;
    jest.clearAllMocks();
  });

  it('fetches products for the selected category and renders the result', async () => {
    const addItemToCart = jest.fn();

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter>
          <Products category="Books" addItemToCart={addItemToCart} />
        </MemoryRouter>,
        container
      );
      await flushPromises();
    });

    expect(global.fetch).toHaveBeenCalledWith('/products/category/Books?limit=12&offset=0');
    expect(container.textContent).toContain('Books');
    expect(container.textContent).toContain('Test Product');

    const addToCartButton = container.querySelector('.price-add');
    Simulate.click(addToCartButton);

    expect(addItemToCart).toHaveBeenCalledWith(product);
  });

  it('disables pagination when there are no more pages to fetch', async () => {
    await act(async () => {
      ReactDOM.render(
        <MemoryRouter>
          <Products category="Books" addItemToCart={jest.fn()} />
        </MemoryRouter>,
        container
      );
      await flushPromises();
    });

    const buttons = Array.from(container.querySelectorAll('button'));
    const previousPageButton = buttons.find(button => button.textContent === 'Previous page');
    const nextPageButton = buttons.find(button => button.textContent === 'Next page');

    expect(previousPageButton.disabled).toBe(true);
    expect(nextPageButton.disabled).toBe(true);
  });

  it('fetches the next and previous pages through the pagination controls', async () => {
    const firstPage = createProducts(12, 1);
    const secondPage = createProducts(1, 13);
    global.fetch = jest.fn()
      .mockImplementationOnce(() => Promise.resolve({ json: () => Promise.resolve(firstPage) }))
      .mockImplementationOnce(() => Promise.resolve({ json: () => Promise.resolve(secondPage) }))
      .mockImplementationOnce(() => Promise.resolve({ json: () => Promise.resolve(firstPage) }));

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter>
          <Products category="Books" addItemToCart={jest.fn()} />
        </MemoryRouter>,
        container
      );
      await flushPromises();
    });

    let buttons = Array.from(container.querySelectorAll('button'));
    let previousPageButton = buttons.find(button => button.textContent === 'Previous page');
    let nextPageButton = buttons.find(button => button.textContent === 'Next page');

    expect(previousPageButton.disabled).toBe(true);
    expect(nextPageButton.disabled).toBe(false);

    await act(async () => {
      Simulate.click(nextPageButton);
      await flushPromises();
    });

    expect(global.fetch).toHaveBeenCalledWith('/products/category/Books?limit=12&offset=12');
    expect(container.textContent).toContain('Product 13');

    buttons = Array.from(container.querySelectorAll('button'));
    previousPageButton = buttons.find(button => button.textContent === 'Previous page');
    nextPageButton = buttons.find(button => button.textContent === 'Next page');

    expect(previousPageButton.disabled).toBe(false);
    expect(nextPageButton.disabled).toBe(true);

    await act(async () => {
      Simulate.click(previousPageButton);
      await flushPromises();
    });

    expect(global.fetch).toHaveBeenCalledWith('/products/category/Books?limit=12&offset=0');
    expect(container.textContent).toContain('Product 1');
  });

  it('refetches when the category prop changes and uses the component link encoding', async () => {
    global.fetch = jest.fn()
      .mockImplementationOnce(() => Promise.resolve({ json: () => Promise.resolve(createProducts(1, 1)) }))
      .mockImplementationOnce(() => Promise.resolve({ json: () => Promise.resolve(createProducts(1, 20)) }));

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter>
          <Products category="Books" addItemToCart={jest.fn()} />
        </MemoryRouter>,
        container
      );
      await flushPromises();
    });

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter>
          <Products category="Kitchen & Dining" addItemToCart={jest.fn()} />
        </MemoryRouter>,
        container
      );
      await flushPromises();
    });

    expect(global.fetch).toHaveBeenCalledWith('/products/category/Kitchen%20%26 Dining?limit=12&offset=0');
    expect(container.textContent).toContain('Kitchen & Dining');
    expect(container.textContent).toContain('Product 20');
  });

  it('sorts rendered products using the requested sort key', async () => {
    const unsortedProducts = [
      {
        id: { asin: 'asin-10' },
        title: 'Higher Reviews',
        price: 10,
        avg_stars: 4,
        num_stars: 4,
        num_reviews: 50,
        imUrl: 'https://example.com/high.png'
      },
      {
        id: { asin: 'asin-11' },
        title: 'Lower Reviews',
        price: 11,
        avg_stars: 4,
        num_stars: 4,
        num_reviews: 10,
        imUrl: 'https://example.com/low.png'
      }
    ];
    global.fetch = jest.fn(() => Promise.resolve({
      json: () => Promise.resolve(unsortedProducts)
    }));

    await act(async () => {
      ReactDOM.render(
        <MemoryRouter>
          <Products category="Books" sort="num_reviews" addItemToCart={jest.fn()} />
        </MemoryRouter>,
        container
      );
      await flushPromises();
    });

    const productNames = Array.from(container.querySelectorAll('.product-name')).map(node => node.textContent);
    expect(productNames).toEqual(['Higher Reviews', 'Lower Reviews']);
  });

  it('returns the expected labels for each supported sort name', () => {
    const products = new Products({});

    expect(products.setSortName('num_stars')).toBe('Books with the Highest Rating');
    expect(products.setSortName('num_reviews')).toBe('Books with the Most Reviews');
    expect(products.setSortName('num_buys')).toBe('Best Selling Books');
    expect(products.setSortName('num_views')).toBe('Books with the Most Pageviews');
    expect(products.setSortName('unknown')).toBe('');
  });
});