import React from 'react';
let Link = require('react-router').Link;


class Breadcrumb extends React.Component {

  render() {
    let path = window.location.pathname.split('/');
    let pages = path.slice(2, path.length);
    let links = [];

    pages.forEach(function(page, i) {
      let link, pageLink;
      if (i !== pages.length - 1) {
        pageLink = pages.slice(0, i + 1).join('/');
        let key = page + i;
        link = <Link key={key} className='crumb' to={`/project/${pageLink}`}>{page}</Link>;
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
