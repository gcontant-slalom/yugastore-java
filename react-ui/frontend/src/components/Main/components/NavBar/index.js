// Dependencies
import React, { Component } from 'react';
import { Logo } from '../';
import { Icon } from '../../../common';
import { NavLink, withRouter } from 'react-router-dom';
// Internals
import './index.css';

class Navbar extends Component {
  state = {itemsInCart: 0}

  componentDidMount() {
    // TODO: add this
    //fetch('http://localhost:3001/cart/itemsInCart')
    //  .then(res => res.json())
    //  .then(itemsInCart => this.setState({ itemsInCart }));
  }

  constructor(props) {
    super(props);
    this.state = {
      itemsInCart: 0,
    };
  }

  render() {
    const { location } = this.props;
    const notIndex = location.pathname!=="/";
    const currentUser = this.props.currentUser;
    const merchantSetupLabel = this.props.merchantContext && this.props.merchantContext.companyName
      ? this.props.merchantContext.companyName + ' Setup'
      : 'Merchant Setup';
    // Always provide a path, fallback to generic if tenantKey missing
    const merchantSetupPath = this.props.merchantContext && this.props.merchantContext.tenantKey
      ? '/' + this.props.merchantContext.tenantKey + '/signup'
      : '/merchant/signup';
    return(
    <nav className={`nav-bar ${this.props.scrolled || notIndex ? 'nav-bar-scrolled' : '' }`}>
      <NavLink to="/">
        <Logo mode={this.props.scrolled || notIndex ? 'dark' : 'light'} />
      </NavLink>
      <div className="nav-links">
        <ul>
          <li><NavLink activeClassName="selected" className="nav-link" to="/Books">
            <span className="nav-link-icon"><Icon icon="book" /></span>
            <span className="nav-link-text">Books</span>
          </NavLink></li>
          <li><NavLink activeClassName="selected" className="nav-link" to="/Music">
            <span className="nav-link-icon"><Icon icon="music" /></span>
            <span className="nav-link-text">Music</span>
          </NavLink></li>
          <li><NavLink activeClassName="selected" className="nav-link" to="/Beauty">
            <span className="nav-link-icon"><Icon icon="makeup" /></span>
            <span className="nav-link-text">Beauty</span>
          </NavLink></li>
          <li><NavLink activeClassName="selected" className="nav-link" to="/Electronics">
            <span className="nav-link-icon"><Icon icon="plug" /></span>
            <span className="nav-link-text">Electronics</span>
          </NavLink></li>
        </ul>
      </div>
      <div className='nav-cart'>
        <NavLink className={`${this.props.cart.total ? 'nav-cart-active' : '' }`} to="/cart">
          {this.props.cart.total > 0 && <span className={`nav-cart-count ${this.props.cart.error ? 'nav-cart-count-error': ''}`}>{this.props.cart.total}</span>}
          <Icon icon="cart" color={this.props.scrolled || notIndex ? '#000000' : '#ffffff' }/>Cart
        </NavLink>
      </div>
      <div className='nav-auth'>
        {currentUser ? (
          <div className="nav-auth-state">
            <NavLink className="nav-auth-link" to={merchantSetupPath}>{merchantSetupLabel}</NavLink>
            <span className="nav-auth-user">{currentUser.email}</span>
            <button className="nav-auth-action" onClick={this.props.onLogout}>Logout</button>
          </div>
        ) : (
          <div className="nav-auth-links">
            <NavLink className="nav-auth-link" to="/login">Sign In</NavLink>
            <NavLink className="nav-auth-link nav-auth-link-primary" to="/register">Register</NavLink>
          </div>
        )}
      </div>
    </nav>
    )
  }
}

export default withRouter(Navbar);
