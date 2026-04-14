import React from 'react';
import ReactDOM from 'react-dom';
import { Simulate } from 'react-dom/test-utils';
import Button from './index';

jest.mock('../', () => ({
  Icon: props => <span className="mock-icon">{props.icon}-{props.color}</span>
}));

describe('Button', () => {
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

  it('renders classes, icon, and click behavior', () => {
    const handleClick = jest.fn();

    ReactDOM.render(
      <Button
        size="large"
        color="primary"
        className="cta"
        icon="cart"
        iconColor="#fff"
        onClick={handleClick}>
        Buy now
      </Button>,
      container
    );

    const button = container.querySelector('button');
    expect(button.className).toContain('btn btn-large btn-primary cta');
    expect(container.textContent).toContain('cart-#fff');

    Simulate.click(button);
    expect(handleClick).toHaveBeenCalled();
  });

  it('supports the disabled state without an icon', () => {
    ReactDOM.render(
      <Button disabled size="small" color="ghost" className="secondary">
        Later
      </Button>,
      container
    );

    const button = container.querySelector('button');
    expect(button.disabled).toBe(true);
    expect(container.querySelector('.mock-icon')).toBeNull();
  });
});