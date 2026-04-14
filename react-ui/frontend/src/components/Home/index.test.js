import React from 'react';
import ReactDOM from 'react-dom';
import { MemoryRouter } from 'react-router-dom';
import Home from './index';

jest.mock('../Main/components', () => ({
  Hero: () => <div>hero-banner</div>
}));

jest.mock('../Products', () => props => (
  <div className="mock-products">
    <span>{props.category}</span>
    <span>{props.limit}</span>
    <span>{String(props.isInline)}</span>
  </div>
));

describe('Home', () => {
  let container;

  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);
  });

  afterEach(() => {
    ReactDOM.unmountComponentAtNode(container);
    document.body.removeChild(container);
    container = null;
    jest.clearAllMocks();
  });

  it('renders the hero and inline bestseller sections', () => {
    ReactDOM.render(
      <MemoryRouter>
        <Home addItemToCart={jest.fn()} />
      </MemoryRouter>,
      container
    );

    expect(container.textContent).toContain('hero-banner');
    expect(container.querySelectorAll('.mock-products')).toHaveLength(4);
    expect(container.textContent).toContain('Books');
    expect(container.textContent).toContain('Music');
    expect(container.textContent).toContain('Beauty');
    expect(container.textContent).toContain('Electronics');

    expect(container.textContent).toContain('4true');
  });
});