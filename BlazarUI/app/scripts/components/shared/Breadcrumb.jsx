import React, {Component, PropTypes} from 'react';
import {values} from 'underscore';
const Link = require('react-router').Link;
import Icon from './Icon.jsx';
import Logo from './Logo.jsx';

class Breadcrumb extends Component {

  render() {

    const appRoot = this.props.appRoot;
    const pages = values(this.props.params);
    const appRootClean = this.props.appRoot.replace(/^\/|\/$/g, '');

    let noLink = false;

    const links = pages.map((page, i) => {
      let pageLinks = '';

      if (i === pages.length - 1) {
        noLink = true;
      }

      for (let g = 0; g < pages.length; g++) {
        pageLinks += `/${pages[g]}`;

        if (i === g) {
          break;
        }
      }

      if (noLink) {
        return (
          <span key={page + i} className='crumb active'>{page}</span>
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
        <Link className='crumb' to='hosts'>
          Hosts
        </Link>
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
