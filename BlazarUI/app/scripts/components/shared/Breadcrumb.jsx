import React, {Component, PropTypes} from 'react';
import {values} from 'underscore';
const Link = require('react-router').Link;
import Icon from './Icon.jsx';
import Logo from './Logo.jsx';
import ClassNames from 'classnames';

class Breadcrumb extends Component {

  render() {

    const appRoot = this.props.appRoot;
    const pages = values(this.props.params);
    const appRootClean = this.props.appRoot.replace(/^\/|\/$/g, '');

    const links = pages.map((page, i) => {
      let pageLinks = '';
      let noLink = false;
      let isActivePage = false;
      let nextCrumb = true;
      let linkToDashboard = false;
  
      // active page
      if (i === pages.length - 1) {
        noLink = true;
        isActivePage = true;
      }
      
      // hosts page
      if (i === 0) {
        noLink = true;
        nextCrumb = false;
      }

      // generate route path for each link
      for (let g = 0; g < pages.length; g++) {
        pageLinks += `/${pages[g]}`;

        if (i === g) {
          break;
        }
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
        <Logo />
        {links}
      </div>
    );

  }
}


Breadcrumb.propTypes = {
  params: PropTypes.object.isRequired,
  appRoot: PropTypes.string.isRequired,
};


export default Breadcrumb;
