import React from 'react';
import ReactDOM from 'react-dom';
import Subscribe from './index';

jest.mock('react-bootstrap', () => ({
  Grid: props => <div className="grid">{props.children}</div>,
  FormGroup: props => <div>{props.children}</div>,
  FormControl: props => <input placeholder={props.placeholder} />
}));

jest.mock('../../../common', () => ({
  Button: props => <button>{props.children}</button>
}));

describe('Subscribe', () => {
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

  it('renders the subscription copy, email input, and button', () => {
    ReactDOM.render(<Subscribe />, container);

    expect(container.textContent).toContain('Let’s keep the conversation going');
    expect(container.textContent).toContain('Receive our newsletter');
    expect(container.querySelector('input').getAttribute('placeholder')).toBe('Email');
    expect(container.querySelector('button').textContent).toBe('Subscribe');
  });
});