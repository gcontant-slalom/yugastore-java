// Dependencies
import _ from 'lodash';
import React, { Component } from 'react';
// Externals
import Cart from '../Cart';
import ShowProduct from '../ShowProduct';
import Products from '../Products';
import Home from '../Home';
import Auth from '../Auth';
import { Navbar, Footer, Subscribe } from '../Main/components';
import { Route, Switch } from 'react-router-dom';
import './index.css';
import 'bootstrap/dist/css/bootstrap.css';

export default class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      cart: {
        data: {},
        total: 0
      },
      currentUser: null,
      authLoaded: false,
      authPending: false,
      authMessage: '',
      scrolled: false,
      index: 0,
    };
  }

  componentWillUnmount() {
    window.removeEventListener('scroll');
  }

  componentDidMount() {
    this.fetchCurrentUser();
  }

  componentWillMount() {
    window.addEventListener('scroll', () =>{
      let supportPageOffset = window.pageXOffset !== undefined;
      let isCSS1Compat = ((document.compatMode || '') === 'CSS1Compat');
      const scroll = {
         x: supportPageOffset ? window.pageXOffset : isCSS1Compat ? document.documentElement.scrollLeft : document.body.scrollLeft,
         y: supportPageOffset ? window.pageYOffset : isCSS1Compat ? document.documentElement.scrollTop : document.body.scrollTop
      };
  
      if(scroll.y > 50 && !this.state.scrolled){
        this.setState({
          scrolled: true
        });
      } else if (scroll.y < 50 && this.state.scrolled) {
        this.setState({
          scrolled: false
        });
      }
    });
  }

  requestJson = (url, options = {}) => {
    const requestOptions = Object.assign({
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      credentials: 'same-origin'
    }, options);

    return fetch(url, requestOptions).then(async res => {
      const text = await res.text();
      const data = text ? JSON.parse(text) : null;
      if (!res.ok) {
        const error = new Error((data && data.message) || 'Request failed');
        error.status = res.status;
        error.data = data;
        throw error;
      }
      return data;
    });
  }

  fetchCurrentUser = () => {
    return this.requestJson('/auth/current-user', { method: 'GET' })
      .then(currentUser => {
        this.setState({ currentUser, authLoaded: true, authMessage: '' });
        return this.fetchCart(currentUser);
      })
      .catch(error => {
        if (error.status === 401) {
          this.setState({
            currentUser: null,
            authLoaded: true,
            authPending: false,
            authMessage: '',
            cart: { data: {}, total: 0, error: false }
          });
          return null;
        }
        this.setState({ authLoaded: true, authMessage: error.message });
        return null;
      });
  }

  fetchCart = () => {
    if (!this.state.currentUser) {
      this.setState({ cart: { data: {}, total: 0, error: false } });
      return Promise.resolve(null);
    }

    return this.requestJson('/cart/get', { method: 'POST' })
      .then(cart => this.setState({
        cart: {
          data: cart,
          total: this.totalReducer(cart),
          error: false
        }
      }))
      .catch(error => {
        if (error.status === 401) {
          this.setState({ currentUser: null, authMessage: 'Please sign in to continue.' });
          return null;
        }
        this.setState({ cart: { ...this.state.cart, error: true } });
        return null;
      });
  }

  totalReducer = (data) => {
    let sum = 0;
    for( var el in data ) {
      if( data.hasOwnProperty( el ) ) {
        sum += parseFloat( data[el] );
      }
    }
    return sum;
  }

  addItemToCart = (product) => {
    if (!this.state.currentUser) {
      this.setState({ authMessage: 'Please sign in before adding items to the cart.' });
      return;
    }
    if (product) {
      console.log("Added to Cart "+product.title);
      
      const url = '/cart/add?asin='+(product.id.asin || product.id);
      this.requestJson(url, { method: 'POST' })
        .then(data => {
            this.setState({
              cart: {
                data: data,
                total: this.totalReducer(data),
                error: false
              },
              authMessage: ''
            });
        })
        .catch(error => {
          if (error.status === 401) {
            this.setState({ currentUser: null, authMessage: 'Please sign in before adding items to the cart.' });
            return;
          }
          this.setState({
            cart: { ...this.state.cart, error: true }
          });
    
          setTimeout(() => this.setState({
            cart: { ...this.state.cart, error: false }
          }), 2500);
          console.warn('Request failed', error);

        });
    }
  }

  removeItemFromCart = (product) => {
    if (!this.state.currentUser) {
      this.setState({ authMessage: 'Please sign in before modifying the cart.' });
      return;
    }
    if (product) {
      console.log("Removed from Cart "+product.title);
      
      const url = '/cart/remove/?asin='+product.id;
      this.requestJson(url, { method: 'POST' })
        .then(data => {
            this.setState({
              cart: {
                data: data,
                total: this.totalReducer(data),
                error: false
              },
              authMessage: ''
            });
        })
        .catch(error => {
          if (error.status === 401) {
            this.setState({ currentUser: null, authMessage: 'Please sign in before modifying the cart.' });
            return;
          }
          this.setState({
            cart: { ...this.state.cart, error: true }
          });
    
          setTimeout(() => this.setState({
            cart: { ...this.state.cart, error: false }
          }), 2500);
          console.warn('Request failed', error);
        });
    }
  }

  login = credentials => {
    this.setState({ authPending: true, authMessage: '' });
    return this.requestJson('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials)
    })
      .then(currentUser => {
        this.setState({ currentUser, authPending: false, authMessage: '' });
        return this.fetchCart();
      })
      .catch(error => {
        this.setState({ authPending: false, authMessage: error.message });
        return null;
      });
  }

  register = payload => {
    this.setState({ authPending: true, authMessage: '' });
    return this.requestJson('/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
      .then(currentUser => {
        this.setState({ currentUser, authPending: false, authMessage: '' });
        return this.fetchCart();
      })
      .catch(error => {
        const fieldErrors = error.data && error.data.fieldErrors ? Object.values(error.data.fieldErrors) : [];
        this.setState({
          authPending: false,
          authMessage: fieldErrors[0] || error.message
        });
        return null;
      });
  }

  logout = () => {
    return this.requestJson('/auth/logout', { method: 'POST' })
      .catch(() => null)
      .then(() => {
        this.setState({
          currentUser: null,
          authPending: false,
          authMessage: '',
          cart: { data: {}, total: 0, error: false }
        });
      });
  }

  render() {
    return(

      <div>
        <Navbar cart={this.state.cart} scrolled={this.state.scrolled} currentUser={this.state.currentUser} onLogout={this.logout} />
        <Switch>
          <Route exact path="/" 
            render={(props) => (
              <Home
                addItemToCart={this.addItemToCart} />
            )} />
      
          <Route path="/cart" 
            render={(props) => (
              <Cart
                cart={this.state.cart} currentUser={this.state.currentUser} fetchCart={this.fetchCart} removeItemFromCart={this.removeItemFromCart}/>
            )} />

          <Route path="/login"
            render={(props) => (
              <Auth
                {...props}
                mode="login"
                authPending={this.state.authPending}
                authMessage={this.state.authMessage}
                onLogin={this.login} />
            )} />

          <Route path="/register"
            render={(props) => (
              <Auth
                {...props}
                mode="register"
                authPending={this.state.authPending}
                authMessage={this.state.authMessage}
                onRegister={this.register} />
            )} />
      
          <Route path="/Music"
            render={(props) => (
              <Products
                category="Music"
                addItemToCart={this.addItemToCart} />
            )} />
          <Route path="/Books"
            render={(props) => (
              <Products
                category={"Books"}
                addItemToCart={this.addItemToCart} />
            )} />
          <Route path="/Beauty"
            render={(props) => (
              <Products
                category={"Beauty"}
                addItemToCart={this.addItemToCart} />
            )} />
          <Route path="/Electronics"
            render={(props) => (
              <Products
                category={"Electronics"}
                addItemToCart={this.addItemToCart} />
            )} />
          <Route exact path="/:category"
            render={(props) => (
              <Products
                {...props}
                addItemToCart={this.addItemToCart} />
            )} />
    
          <Route path="/sort/:query"
            render={(props) => (
              <Products
                sort={props.match.params.query}
                addItemToCart={this.addItemToCart} />
            )} />
      
          <Route exact path="/item/:id" render={(props) => (
            <ShowProduct {...props} addItemToCart={this.addItemToCart}/>
          )}/>
        </Switch>
        <Subscribe/>
        <Footer />
      </div>
    )
  }
}
