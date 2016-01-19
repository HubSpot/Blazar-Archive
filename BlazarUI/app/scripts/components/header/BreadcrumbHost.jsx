import React, {Component, PropTypes} from 'react';
const Link = require('react-router').Link;
import Icon from '../shared/Icon.jsx';
import {bindAll} from 'underscore';
import Loader from '../shared/Loader.jsx';
import ClassNames from 'classnames';

class BreadcrumbHost extends Component {

  constructor() {
    bindAll(this, 'handleOrgClick', 'handleLinkClick');

    this.state = {
      active: false
    }
  }
  
  handleOrgClick() {
    this.setState({
      active: !this.state.active
    });
  }

  handleLinkClick() {
    this.setState({
      active: false
    });
  }
  
  getContainerClassNames() {
    return ClassNames([
      'crumb',
      'org-nav',
      {'active': this.props.isActive}
    ])
  }

  render() {
    let HostsWithOrgs;
    
    if (this.props.loadingHosts) {
      return null;
    }

    const orgItems = this.props.hosts.map((host, i) => {

      const orgs = host.get('orgs').map((org, i) => {
        return (
          <li key={i}>
            <Link to={org.get('blazarPath')} onClick={this.handleLinkClick}>
              {org.get('name')}
            </Link>
          </li>
        );
      });

      return (
        <div key={i} className='org-nav__host'>
          {host.get('name')}
          <ul className='org-nav__host-orgs'>
            {orgs}
          </ul>
        </div>
      );
    });

    if (this.state.active) {
      let loader;
      if (orgItems.size === 0) {
        loader = (
          <Loader align='center' />
        );
      }

      HostsWithOrgs = (
        <div className='org-nav__links'>
          {orgItems}
          {loader}
        </div>
      )
    }

    return (
      <span className={this.getContainerClassNames()}>
        <span className='org-nav__headline' onClick={this.handleOrgClick}>
          <Icon name='caret-down' />
          <Icon classNames='breadcrumb-icon' for='org' />
          {this.props.params.org}
        </span>
        {HostsWithOrgs}
      </span>
    )

  }
}

BreadcrumbHost.propTypes = {
  hosts: PropTypes.object,
  page: PropTypes.string
};


export default BreadcrumbHost;
