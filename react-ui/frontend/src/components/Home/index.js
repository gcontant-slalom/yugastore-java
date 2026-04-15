// Dependencies
import React, { Component } from 'react';
import { Link } from 'react-router-dom';
//Internals
import { Hero } from '../Main/components';
import Products from '../Products';
import './index.css';

class Home extends Component {
  constructor(props) {
    super(props);
    this.state = {
      cart: {},
    };
  }

  render() {
    const tenantContext = this.props.tenantContext;
    const tenantPrefix = tenantContext ? '/' + tenantContext.tenantKey : '';

    return (
      <div>
        {tenantContext && (
          <div className="tenant-storefront-banner">
            <div className="tenant-storefront-eyebrow">Tenant Storefront</div>
            <h1>{tenantContext.companyName}</h1>
            <p>Browsing storefront /{tenantContext.tenantKey}</p>
          </div>
        )}
        <Hero/>
        <div className="paragraph">
          <Products addItemToCart={this.props.addItemToCart} isInline={true} tenantKey={tenantContext && tenantContext.tenantKey} name={<span>Bestsellers in <Link to={tenantPrefix + "/Books"}>Books</Link></span>} category="Books" limit={4}/>
          <Products addItemToCart={this.props.addItemToCart} isInline={true} tenantKey={tenantContext && tenantContext.tenantKey} name={<span>Bestsellers in <Link to={tenantPrefix + "/Music"}>Music</Link></span>} category="Music" limit={4}/>
          <Products addItemToCart={this.props.addItemToCart} isInline={true} tenantKey={tenantContext && tenantContext.tenantKey} name={<span>Bestsellers in <Link to={tenantPrefix + "/Beauty"}>Beauty</Link></span>} category="Beauty" limit={4}/>
          <Products addItemToCart={this.props.addItemToCart} isInline={true} tenantKey={tenantContext && tenantContext.tenantKey} name={<span>Bestsellers in <Link to={tenantPrefix + "/Electronics"}>Electronics</Link></span>} category="Electronics" limit={4}/>
        </div>
      </div>
    );
  }
}

export default Home;
