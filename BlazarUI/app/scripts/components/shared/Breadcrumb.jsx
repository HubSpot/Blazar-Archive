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

    const links = pages.map((page, i) => {
      let pageLinks = '';
      for (let i = 0; i < pages.length; i++) {
        pageLinks += `/${pages[i]}`;
        if (page === pages[i]) {
          break;
        }
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
