import React from 'react';
import ReactDOM from 'react-dom';
import Logo from './index';

describe('Logo', () => {
  let container;

  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);
  });

  afterEach(() => {
    ReactDOM.unmountComponentAtNode(container);
    document.body.removeChild(container);
    container = null;
  });

  it('renders the light logo variant', () => {
    ReactDOM.render(<Logo mode="light" />, container);

    expect(container.querySelector('.logo')).not.toBeNull();
    expect(container.innerHTML).toContain('fill="#FFFFFF"');
  });

  it('renders the default dark logo variant when mode is not light', () => {
    ReactDOM.render(<Logo mode="dark" />, container);

    expect(container.innerHTML).toContain('fill="#2A3033"');
  });
});