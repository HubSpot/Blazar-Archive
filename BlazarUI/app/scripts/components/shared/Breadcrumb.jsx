import React, {Component, PropTypes} from 'react';
const Link = require('react-router').Link;
import Icon from './Icon.jsx';

class Breadcrumb extends Component {

  render() {
    const appRootSplit = this.props.appRoot.split('/');
    let path = this.props.path.split('/');
    path.shift()

    const pages = path.slice(appRootSplit.length, path.length);

    let links = [];

    pages.forEach(function(page, i) {
      let link;
      const key = page + i;

      if (i !== pages.length - 1 && i !== 0) {
        const pageLink = pages.slice(0, i + 1).join('/');
        link = <Link key={key} className='crumb' to={`${this.props.appRoot}/builds/${pageLink}`}>{page}</Link>;
      } else if (i === 0) {
        link = <span key={key} className='crumb'>{page}</span>
      } else {
        link = <span key={key} className='crumb-active'> {page} </span>
      }

      links.push(link);

    }.bind(this));

    return (
      <div className='breadcrumbs'> 
        <Link className='crumb' to='dashboard'> 
          <Icon name='home' />
        </Link>
        {links} 
      </div>
    );

  }
}


Breadcrumb.propTypes = {
  path: PropTypes.string.isRequired,
  appRoot: PropTypes.string.isRequired,
};


export default Breadcrumb;
