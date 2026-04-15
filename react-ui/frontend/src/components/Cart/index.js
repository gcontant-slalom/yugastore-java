//Dependencies
import React, { Component } from 'react';
import { Button } from '../../components/common';
import { Link } from 'react-router-dom';
//Internals
import './index.css';

class CartProducts extends Component {
  constructor(props) {
    super(props);
    this.state = {
      products: [],
      isCompleted: false,
      resolvedCartTenantContext: null,
      checkoutError: ''
    };
  }

  componentDidMount() {
    const loadContext = this.props.currentUser && this.props.cart.total && !this.props.cartTenantContext
      ? this.loadCartTenantContext()
      : Promise.resolve(this.getEffectiveCartTenantContext());

    loadContext.then(() => {
      Object.keys(this.props.cart.data).forEach(product_id => this.fetchProductDetails(product_id));
    });
  }

  submitCheckout() {
    if (this.props.cart.total !== 0) {
      this.loadCartTenantContext().then(tenantContext => {
        var url = '/cart/checkout';
        const headers = {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        };
        if (tenantContext && tenantContext.tenantKey) {
          headers['X-Tenant-Key'] = tenantContext.tenantKey;
        }
        if (tenantContext && tenantContext.companyName) {
          headers['X-Merchant-Company-Name'] = tenantContext.companyName;
        }
        fetch(url, {
          method: 'POST',
          headers
        })
          .then(async res => {
            const text = await res.text();
            const payload = text ? JSON.parse(text) : null;
            if (!res.ok) {
              this.setState({ checkoutError: (payload && payload.message) || 'Checkout requires tenant context for this cart.' });
              return null;
            }
            this.setState({ result: payload, isCompleted: true, checkoutError: '' });
            return this.props.fetchCart();
          });
      });
    }
  }

  getEffectiveCartTenantContext = () => {
    return this.props.cartTenantContext || this.state.resolvedCartTenantContext;
  }

  loadCartTenantContext = () => {
    const currentContext = this.getEffectiveCartTenantContext();
    if (currentContext || !this.props.currentUser || !this.props.cart.total) {
      return Promise.resolve(currentContext);
    }

    return fetch('/cart/tenant-context', {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    })
      .then(async res => {
        if (!res.ok) {
          return null;
        }
        const text = await res.text();
        const tenantContext = text ? JSON.parse(text) : null;
        const resolvedCartTenantContext = tenantContext && tenantContext.tenantKey ? tenantContext : null;
        this.setState({ resolvedCartTenantContext });
        return resolvedCartTenantContext;
      })
      .catch(() => null);
  }

  costReducer = (accumulator, currentValue) => {
    return accumulator + currentValue.price * parseInt(this.props.cart.data[currentValue.id] || 0, 10);
  }

  fetchProductDetails(product_id) {
    if (!this.state.products.find(product => (product.id.asin || product.id) === product_id)) {
      const tenantContext = this.getEffectiveCartTenantContext();
      const tenantKey = tenantContext && tenantContext.tenantKey;
      var url = tenantKey
        ? '/tenant/' + tenantKey + '/products/details?asin=' + product_id
        : '/products/details?asin=' + product_id;
      console.log("Fetching url: " + url);
      fetch(url)
        .then(res => res.json())
        .then(product => this.setState({ 
          products: this.state.products.concat([product])
        })
      );
    }
  }
  render() {
    if (!this.props.currentUser) {
      return(
        <div className="cart-container">
          <div className="container">
            <h5>Items in cart</h5>
            <h6>Please sign in to view and modify your cart.</h6>
            <Link to="/login">Go to sign in</Link>
          </div>
        </div>
      );
    }
    const totalCost = this.state.products.length ? this.state.products.reduce(this.costReducer, 0) : 0;
    return(
      <div className="cart-container">
      <div className="container">
        <h5>{ this.state.isCompleted ? "Thank you!" : "Items in cart" } {Boolean(this.props.cart.total) && <span className="total-in-cart">({this.props.cart.total})</span>}</h5>
          { this.state.products && <div className="items">
          { this.state.products.filter((product) => this.props.cart.data[product.id]).map(product => (
              <div key={product.id} className="cart-item">
                <div className="product-image">
                  <img src={product.imUrl} alt="product" />
                </div>
                <div className="details">
                  <Link to={`/item/${product.id.asin || product.id}`}>{product.title}</Link>
                </div>
    
                <div className="pricing">
                  <h6>${product.price.toFixed(2)}</h6> x {this.props.cart.data[product.id]}
                </div>
                <div className="actions">
                  <Button className="btn-cart-remove" onClick={() => this.props.removeItemFromCart(product)}  size="meduim">Remove</Button>
                </div>
              </div>
            ))}
        </div>}
        { !this.props.cart.total &&
          <h6>{ this.state.isCompleted ? <span>Your order <b>#kmp-{this.state.result.orderNumber}</b> is received.</span> : "Cart is empty"}</h6>
        }
        { Boolean(this.state.isCompleted) && this.state.result &&
          <div className="order-details">{this.state.result.orderDetails}</div>
        }
        { Boolean(this.state.checkoutError) &&
          <div className="order-details">{this.state.checkoutError}</div>
        }
        { Boolean(this.props.cart.total) && 
            <div className="total">
              <div className="details">
              </div>

              <div className="pricing">
                Total:<br/>
                Taxes:
              </div>
              <div className="pricing">
                <h6>${totalCost.toFixed(2)}</h6>
                <h6>$0.00</h6>
              </div>
              <div className="actions">
              <Button onClick={() => this.submitCheckout()} size="meduim" disabled={!Boolean(this.props.cart.total)}>Checkout</Button>
              </div>
            </div>
        }
      </div>
      </div>
    );
  }
}

export default CartProducts;
