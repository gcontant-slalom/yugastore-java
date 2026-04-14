import React from 'react';
import ReactDOM from 'react-dom';
import { MemoryRouter } from 'react-router-dom';
import Navbar from './index';

jest.mock('../', () => ({
  Logo: props => <div className="mock-logo">logo-{props.mode}</div>
}));

jest.mock('../../../common', () => ({
  Icon: props => <span className="mock-icon">{props.icon}-{props.color || 'none'}</span>
}));

describe('Navbar', () => {
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

  it('uses the light logo on the home route when not scrolled', () => {
    ReactDOM.render(
      <MemoryRouter initialEntries={['/']}>
        <Navbar cart={{ total: 0, error: false }} scrolled={false} />
      </MemoryRouter>,
      container
    );

    const nav = container.querySelector('nav');
    expect(nav.className).toBe('nav-bar ');
    expect(container.textContent).toContain('logo-light');
    expect(container.textContent).not.toContain('nav-cart-count');
  });

  it('shows a scrolled navbar state and active cart count away from home', () => {
    ReactDOM.render(
      <MemoryRouter initialEntries={['/Books']}>
        <Navbar cart={{ total: 2, error: true }} scrolled={false} />
      </MemoryRouter>,
      container
    );

    const nav = container.querySelector('nav');
    expect(nav.className).toContain('nav-bar-scrolled');
    expect(container.textContent).toContain('logo-dark');
    expect(container.textContent).toContain('2');

    const cartCount = container.querySelector('.nav-cart-count');
    expect(cartCount.className).toContain('nav-cart-count-error');
  });
});