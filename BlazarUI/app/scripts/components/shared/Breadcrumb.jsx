import React, {Component, PropTypes} from 'react';
const Link = require('react-router').Link;


class Breadcrumb extends Component {

  render() {
    const appRootSplit = this.props.appRoot.split('/');
    let path = this.props.path.split('/');
    path.shift()

    const pages = path.slice(appRootSplit.length, path.length);

    let links = [];

    pages.forEach(function(page, i) {
      let link, key;
      if (i !== pages.length - 1 && i !== 0) {
        const pageLink = pages.slice(0, i + 1).join('/');
        key = page + i;
        link = <Link key={key} className='crumb' to={`/builds/${pageLink}`}>{page}</Link>;
      } else if (i === 0) {
        link = page + ' / ';
      } else {
        link = page;
      }
      links.push(link);
    }.bind(this));

    return (
      <div className='breadcrumbs'> {links} </div>
    );

  }
}


Breadcrumb.propTypes = {
  path: PropTypes.string.isRequired,
  appRoot: PropTypes.string.isRequired,
};


export default Breadcrumb;
