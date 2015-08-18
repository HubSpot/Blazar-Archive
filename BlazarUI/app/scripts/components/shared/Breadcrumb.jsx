/*global config*/
import React, {Component} from 'react';
let Link = require('react-router').Link;


class Breadcrumb extends Component {

  render() {
    let path = window.location.pathname.split('/');
    let pages = path.slice(4, path.length);
    let links = [];

    pages.forEach(function(page, i) {
      let link, pageLink, key;
      if (i !== pages.length - 1 && i !== 0) {
        pageLink = pages.slice(0, i + 1).join('/');
        key = page + i;
        link = <Link key={key} className='crumb' to={`${config.appRoot}/builds/${pageLink}`}>{page}</Link>;
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
