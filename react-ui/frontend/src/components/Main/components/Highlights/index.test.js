import React from 'react';
import ReactDOM from 'react-dom';
import { MemoryRouter } from 'react-router-dom';
import Highlights from './index';

describe('Highlights', () => {
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

  it('renders links for each sort highlight', () => {
    ReactDOM.render(
      <MemoryRouter>
        <Highlights />
      </MemoryRouter>,
      container
    );

    const links = Array.from(container.querySelectorAll('a'));
    expect(links.map(link => link.textContent)).toEqual([
      'Highest Rating',
      'Most Reviews',
      'Best Selling',
      'Most Viewed'
    ]);
    expect(links.map(link => link.getAttribute('href'))).toEqual([
      '/sort/num_stars',
      '/sort/num_reviews',
      '/sort/num_buys',
      '/sort/num_views'
    ]);
  });
});