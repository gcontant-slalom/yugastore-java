import React from 'react';
import ReactDOM from 'react-dom';
import { MemoryRouter } from 'react-router-dom';
import Footer from './index';

jest.mock('../', () => ({
  Logo: () => <div>footer-logo</div>
}));

describe('Footer', () => {
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

  it('renders category links with encoded paths', () => {
    ReactDOM.render(
      <MemoryRouter>
        <Footer />
      </MemoryRouter>,
      container
    );

    expect(container.textContent).toContain('footer-logo');
    expect(container.textContent).toContain('© Copyright Yugabyte 2018');

    const links = Array.from(container.querySelectorAll('a')).map(link => ({
      text: link.textContent,
      href: link.getAttribute('href')
    }));

    expect(links).toEqual(expect.arrayContaining([
      { text: 'Books', href: '/Books' },
      { text: 'Kitchen & Dining', href: '/Kitchen %26 Dining' },
      { text: 'Movies & TV', href: '/Movies %26 TV' }
    ]));
  });
});