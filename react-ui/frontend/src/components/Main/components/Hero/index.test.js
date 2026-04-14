import React from 'react';
import ReactDOM from 'react-dom';
import Hero from './index';

describe('Hero', () => {
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

  it('renders the hero wrapper and background image', () => {
    ReactDOM.render(<Hero />, container);

    expect(container.querySelector('.hero')).not.toBeNull();
    expect(container.querySelector('img').getAttribute('alt')).toBe('background-image');
  });
});