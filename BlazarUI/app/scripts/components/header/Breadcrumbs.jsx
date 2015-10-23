import React, {Component, PropTypes} from 'react';
import {values, pick} from 'underscore';
const Link = require('react-router').Link;
import Icon from '../shared/Icon.jsx';
import Logo from '../shared/Logo.jsx';
import ClassNames from 'classnames';


const ICON_MAP = {
  1: 'org',
  2: 'repo',
  3: 'branch',
  4: 'module'
};

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

      // repo and org pages
      if (i === 0 || i === 1) {
        noLink = true;
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
          <span key={page + i} className={classNames}>
            { ICON_MAP[i] ? <Icon classNames='breadcrumb-icon' for={ICON_MAP[i]} />  : null}
            {page}
          </span>
        );        
      }
      
      
      return (
        <Link key={page + i} className='crumb' to={`${this.props.appRoot}/builds${pageLinks}`}>
          { ICON_MAP[i] ? <Icon classNames='breadcrumb-icon' for={ICON_MAP[i]} />  : null}
          {page}
        </Link>
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
  navigationIsActive: PropTypes.bool
};


export default Breadcrumbs;
