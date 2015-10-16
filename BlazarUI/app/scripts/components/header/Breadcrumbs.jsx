import React, {Component, PropTypes} from 'react';
import {values, pick} from 'underscore';
const Link = require('react-router').Link;
import Icon from '../shared/Icon.jsx';
import Logo from '../shared/Logo.jsx';
import ClassNames from 'classnames';
import HostDropdownBreadcrumb from './HostDropdownBreadcrumb.jsx';

class Breadcrumbs extends Component {

  render() {

    const appRoot = this.props.appRoot;  
    //remove any unwanted params that may have been added
    const cleanParams = pick(this.props.params, 'host','org','repo','branch','module','buildNumber')
    const pages = values(cleanParams);

    const links = pages.map((page, i) => {
      let pageLinks = '';
      let noLink = false;
      let isActivePage = false;
      let nextCrumb = true;
      let linkToDashboard = false;
      let isOrg;
  
      // active page
      if (i === pages.length - 1) {
        noLink = true;
        isActivePage = true;
      }

      // repo 
      if (i === 0) {
        noLink = true;
      }

      // org page
      if (i === 1) {
        isOrg = true;
      }

      // generate route path for each link
      for (let g = 0; g < pages.length; g++) {
        pageLinks += `/${pages[g]}`;

        if (i === g) {
          break;
        }
      }
      
      if (isOrg) {
        
        return (
          <HostDropdownBreadcrumb 
            key={i}
            page={page}
            hosts={this.props.hosts} 
            navigationIsActive={this.props.navigationIsActive}
            activePage={isActivePage}
          />
        );
      }

      if (noLink) {
        const classNames = ClassNames([
          'crumb',
          {'active': isActivePage}
        ]);
        
        return (
          <span key={page + i} className={classNames}>{page}</span>
        );        
      }
      
      
      return (
        <Link key={page + i} className='crumb' to={`${this.props.appRoot}/builds${pageLinks}`}>{page}</Link>
      );

    })

    if (links.length === 0) {
      return (
        <div className='breadcrumbs'>
        <Logo crumb={false} />
        </div>
      )
    }

    return (
      <div className='breadcrumbs'>
        <Logo crumb={false} />
        {links}
      </div>
    );

  }
}


Breadcrumbs.propTypes = {
  params: PropTypes.object.isRequired,
  appRoot: PropTypes.string.isRequired,
  loadingHosts: PropTypes.bool,
  hosts: PropTypes.array,
  navigationIsActive: PropTypes.bool
};


export default Breadcrumbs;
