import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../../components/common';
import './index.css';

class Auth extends Component {
  constructor(props) {
    super(props);
    this.state = {
      email: '',
      password: '',
      passwordConfirm: '',
      formError: ''
    };
  }

  componentDidUpdate(prevProps) {
    if (prevProps.mode !== this.props.mode) {
      this.setState({
        email: '',
        password: '',
        passwordConfirm: '',
        formError: ''
      });
    }
  }

  handleChange = event => {
    this.setState({ [event.target.name]: event.target.value, formError: '' });
  }

  submit = event => {
    event.preventDefault();
    const { email, password, passwordConfirm } = this.state;
    const isRegister = this.props.mode === 'register';

    if (!email || !password || (isRegister && !passwordConfirm)) {
      this.setState({ formError: 'All required fields must be completed.' });
      return;
    }

    if (isRegister && password !== passwordConfirm) {
      this.setState({ formError: 'These passwords do not match.' });
      return;
    }

    if (isRegister) {
      this.props.onRegister({ email, password, passwordConfirm });
      return;
    }

    this.props.onLogin({ email, password });
  }

  render() {
    const isRegister = this.props.mode === 'register';
    const authMessage = this.state.formError || this.props.authMessage;

    return (
      <div className="auth-page">
        <div className="auth-card">
          <div className="auth-eyebrow">Merchant Access</div>
          <h1>{isRegister ? 'Create your account' : 'Sign in to continue'}</h1>
          <p className="auth-subtitle">
            {isRegister ? 'Register with your email address to start managing your cart and checkout.'
              : 'Use your email address and password to access protected cart and checkout flows.'}
          </p>
          {authMessage && <div className="auth-message">{authMessage}</div>}
          <form className="auth-form" onSubmit={this.submit}>
            <label>Email</label>
            <input name="email" type="email" value={this.state.email} onChange={this.handleChange} />
            <label>Password</label>
            <input name="password" type="password" value={this.state.password} onChange={this.handleChange} />
            {isRegister && (
              <div>
                <label>Confirm password</label>
                <input name="passwordConfirm" type="password" value={this.state.passwordConfirm} onChange={this.handleChange} />
              </div>
            )}
            <Button color="primary" size="large" className="auth-submit" disabled={this.props.authPending}>
              {this.props.authPending ? 'Working...' : isRegister ? 'Create Account' : 'Sign In'}
            </Button>
          </form>
          <div className="auth-switch">
            {isRegister ? <span>Already have an account? <Link to="/login">Sign in</Link></span>
              : <span>Need an account? <Link to="/register">Create one</Link></span>}
          </div>
        </div>
      </div>
    );
  }
}

export default Auth;