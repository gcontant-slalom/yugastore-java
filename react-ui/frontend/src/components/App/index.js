// Dependencies
import React, { Component } from 'react';
// Externals
import Cart from '../Cart';
import ShowProduct from '../ShowProduct';
import Products from '../Products';
import Home from '../Home';
import Auth from '../Auth';
import MerchantSignup from '../MerchantSignup';
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
      merchantContexts: [],
      merchantContext: null,
      authLoaded: false,
      authPending: false,
      authMessage: '',
      merchantSignupPending: false,
      merchantSignupMessage: '',
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
        return Promise.all([this.fetchCart(), this.fetchMerchantContexts()]);
      })
      .catch(error => {
        if (error.status === 401) {
          this.setState({
            currentUser: null,
            merchantContexts: [],
            merchantContext: null,
            authLoaded: true,
            authPending: false,
            authMessage: '',
            merchantSignupPending: false,
            merchantSignupMessage: '',
            cart: { data: {}, total: 0, error: false }
          });
          return null;
        }
        this.setState({ authLoaded: true, authMessage: error.message });
        return null;
      });
  }

  fetchMerchantContexts = () => {
    if (!this.state.currentUser) {
      this.setState({ merchantContexts: [], merchantContext: null });
      return Promise.resolve(null);
    }

    return this.requestJson('/api/v1/merchant-context/list', { method: 'GET' })
      .then(merchantContexts => {
        const tenantList = Array.isArray(merchantContexts) ? merchantContexts : [];
        this.setState({
          merchantContexts: tenantList,
          merchantContext: tenantList.length > 0 ? tenantList[tenantList.length - 1] : null,
          merchantSignupMessage: ''
        });
        return tenantList;
      })
      .catch(error => {
        if (error.status === 404) {
          this.setState({ merchantContexts: [], merchantContext: null });
          return null;
        }
        this.setState({ merchantSignupMessage: error.message });
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
      console.log('Added to Cart '+product.title);

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
      console.log('Removed from Cart '+product.title);

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
        this.setState({ currentUser, authPending: false, authMessage: '', merchantSignupMessage: '' });
        return Promise.all([this.fetchCart(), this.fetchMerchantContexts()]);
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
        this.setState({ currentUser, authPending: false, authMessage: '', merchantSignupMessage: '' });
        return Promise.all([this.fetchCart(), this.fetchMerchantContexts()]);
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
          merchantContexts: [],
          merchantContext: null,
          authPending: false,
          authMessage: '',
          merchantSignupPending: false,
          merchantSignupMessage: '',
          cart: { data: {}, total: 0, error: false }
        });
      });
  }

  createMerchantSignup = payload => {
    if (!this.state.currentUser) {
      this.setState({ merchantSignupMessage: 'Please sign in before creating a merchant tenant.' });
      return Promise.resolve(null);
    }

    this.setState({ merchantSignupPending: true, merchantSignupMessage: '' });
    return this.requestJson('/api/v1/merchant-signup', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
      .then(merchantContext => {
        this.setState(prevState => ({
          merchantContexts: [...prevState.merchantContexts, merchantContext],
          merchantContext,
          merchantSignupPending: false,
          merchantSignupMessage: ''
        }));
        return merchantContext;
      })
      .catch(error => {
        this.setState({ merchantSignupPending: false, merchantSignupMessage: error.message });
        return null;
      });
  }

  render() {
    return (
      <div>
        <Navbar
          scrolled={this.state.scrolled}
          cart={this.state.cart}
          currentUser={this.state.currentUser}
          onLogout={this.logout} />

        <Switch>
          <Route exact path="/" render={() => <Home addItemToCart={this.addItemToCart} />} />
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
          <Route path="/merchant/signup"
            render={(props) => (
              <MerchantSignup
                {...props}
                currentUser={this.state.currentUser}
                pending={this.state.merchantSignupPending}
                message={this.state.merchantSignupMessage}
                merchantContexts={this.state.merchantContexts}
                merchantContext={this.state.merchantContext}
                onSubmit={this.createMerchantSignup} />
            )} />
          <Route path="/cart" render={() => (
            <Cart
              cart={this.state.cart}
              currentUser={this.state.currentUser}
              removeItemFromCart={this.removeItemFromCart}
              fetchCart={this.fetchCart} />
          )} />
          <Route path="/item/:asin" render={(props) => <ShowProduct {...props} addItemToCart={this.addItemToCart} />} />
          <Route path="/sort/:sort" render={(props) => <Products {...props} sort={props.match.params.sort} addItemToCart={this.addItemToCart} />} />
          <Route exact path="/:category(Books|Music|Beauty|Electronics)" render={(props) => <Products {...props} category={props.match.params.category} addItemToCart={this.addItemToCart} />} />
          <Route path="/:category" render={(props) => <Products {...props} addItemToCart={this.addItemToCart} />} />
        </Switch>
        <Subscribe />
        <Footer />
      </div>
    );
  }
}
