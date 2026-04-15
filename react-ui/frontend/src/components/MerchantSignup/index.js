import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../../components/common';
import './index.css';

class MerchantSignup extends Component {
  constructor(props) {
    super(props);
    this.state = {
      companyName: '',
      tenantKey: '',
      formError: ''
    };
  }

  handleChange = event => {
    this.setState({ [event.target.name]: event.target.value, formError: '' });
  }

  submit = event => {
    event.preventDefault();
    const { companyName, tenantKey } = this.state;

    if (!this.props.currentUser) {
      this.setState({ formError: 'Please sign in before creating a merchant tenant.' });
      return;
    }

    if (!companyName || !tenantKey) {
      this.setState({ formError: 'Company or store name and tenant key are required.' });
      return;
    }

    this.props.onSubmit({ companyName, tenantKey });
  }

  renderSuccess() {
    const merchantContext = this.props.merchantContext;
    if (!merchantContext) {
      return null;
    }

    return (
      <div className="merchant-signup-success">
        <h2>Merchant tenant created</h2>
        <p>{merchantContext.companyName} is now linked to your account.</p>
        <p className="merchant-signup-meta">Tenant key: <strong>{merchantContext.tenantKey}</strong></p>
      </div>
    );
  }

  renderTenantList() {
    const merchantContexts = this.props.merchantContexts || [];
    if (!this.props.currentUser || merchantContexts.length === 0) {
      return null;
    }

    return (
      <div className="merchant-tenant-list">
        <h2>Your tenants</h2>
        <ul>
          {merchantContexts.map(tenant => (
            <li key={tenant.tenantId || tenant.tenantKey}>
              <span className="merchant-tenant-name">{tenant.companyName}</span>
              <span className="merchant-tenant-key">{tenant.tenantKey}</span>
            </li>
          ))}
        </ul>
      </div>
    );
  }

  render() {
    const message = this.state.formError || this.props.message;

    return (
      <div className="auth-page merchant-signup-page">
        <div className="auth-card merchant-signup-card">
          <div className="auth-eyebrow">Merchant Onboarding</div>
          <h1>Create your merchant tenant</h1>
          <p className="auth-subtitle">
            Create the shared merchant company context and link your current authenticated account as the first merchant admin.
          </p>
          {!this.props.currentUser && (
            <div className="auth-message">Sign in first, then return to this onboarding route. <Link to="/login">Go to sign in</Link></div>
          )}
          {message && <div className="auth-message">{message}</div>}
          {this.renderSuccess()}
          {this.renderTenantList()}
          <form className="auth-form" onSubmit={this.submit}>
            <label>Company or store name</label>
            <input name="companyName" type="text" value={this.state.companyName} onChange={this.handleChange} />
            <label>Tenant key</label>
            <input name="tenantKey" type="text" value={this.state.tenantKey} onChange={this.handleChange} />
            <Button color="primary" size="large" className="auth-submit" disabled={this.props.pending || !this.props.currentUser}>
              {this.props.pending ? 'Creating tenant...' : 'Create merchant tenant'}
            </Button>
          </form>
        </div>
      </div>
    );
  }
}

export default MerchantSignup;
