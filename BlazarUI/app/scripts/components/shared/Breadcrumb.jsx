import React, {Component, PropTypes} from 'react';
const Link = require('react-router').Link;


class Breadcrumb extends Component {

  render() {
    const path = this.props.path.split('/');
    const pages = path.slice(2, path.length);
    const links = [];

    pages.forEach(function(page, i) {
      let link, key;
      if (i !== pages.length - 1 && i !== 0) {
        const pageLink = pages.slice(0, i + 1).join('/');
        key = page + i;
        link = <Link key={key} className='crumb' to={`${this.props.appRoot}/builds/${pageLink}`}>{page}</Link>;
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
