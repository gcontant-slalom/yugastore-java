import React from 'react';
import ReactDOM from 'react-dom';
import Main from './index';

jest.mock('./components', () => ({
  Navbar: () => <div>main-navbar</div>,
  Footer: () => <div>main-footer</div>
}));

describe('Main', () => {
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

  it('renders navbar, children, and footer', () => {
    ReactDOM.render(
      <Main>
        <div>child-content</div>
      </Main>,
      container
    );

    expect(container.textContent).toContain('main-navbar');
    expect(container.textContent).toContain('child-content');
    expect(container.textContent).toContain('main-footer');
  });
});