import React from 'react';
import ReactDOM from 'react-dom';
import Icon from './index';

describe('Icon', () => {
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

  it('renders font-awesome based icons for simple variants', () => {
    const cases = [
      ['dollar', 'fa-dollar-sign'],
      ['music', 'fa-music'],
      ['camera', 'fa-plug'],
      ['rocket', 'fa-rocket']
    ];

    cases.forEach(([name, className]) => {
      ReactDOM.render(<Icon icon={name} />, container);
      expect(container.querySelector('i').className).toContain(className);
    });
  });

  it('renders svg-based icons and applies the custom color when provided', () => {
    const cases = ['art', 'makeup', 'cookbook', 'book', 'scifi', 'cart', 'cart-add'];

    cases.forEach(name => {
      ReactDOM.render(<Icon icon={name} color="#123456" />, container);
      expect(container.querySelector('svg')).not.toBeNull();
      expect(container.innerHTML).toContain('#123456');
    });
  });

  it('renders the default font-awesome fallback when no icon is provided', () => {
    ReactDOM.render(<Icon />, container);

    expect(container.querySelector('i').className).toContain('fa-undefined');
  });
});