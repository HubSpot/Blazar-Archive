/*global config*/
import React, {Component} from 'react';
const Link = require('react-router').Link;


class Breadcrumb extends Component {

  render() {
    const path = window.location.pathname.split('/');
    const pages = path.slice(2, path.length);
    const links = [];

    pages.forEach(function(page, i) {
      let link, key;
      if (i !== pages.length - 1 && i !== 0) {
        const pageLink = pages.slice(0, i + 1).join('/');
        key = page + i;
        link = <Link key={key} className='crumb' to={`${config.appRoot}builds/${pageLink}`}>{page}</Link>;
      } else if (i === 0) {
        link = page + ' / ';
      } else {
        link = page;
      }
      links.push(link);
    });

    return (
      <div className='breadcrumbs'> {links} </div>
    );

  }
}

export default Breadcrumb;
