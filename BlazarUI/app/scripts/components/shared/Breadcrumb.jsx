import React from 'react';

class Breadcrumb extends React.Component{

  constructor(props, context) {
   super(props);
  }

  render() {

    let crumbsep = "/";
    let path = "<a href='/'>Dashboard</a> » ";

    if(! this.props.links){
      return <div></div>
    }

    this.props.links.forEach( (link, index) => {

      if(index === this.props.links.length - 1){
        path += `<span>${link.title}</span>`
      }
      else{
       path += `<a href='${link.link}'>${link.title}</a>${crumbsep}`
      }

    })

    return (
      <div className='breadcrumbs' dangerouslySetInnerHTML={{__html: path}}></div>
    );
  }
}

export default Breadcrumb;