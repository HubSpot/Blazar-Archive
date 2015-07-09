import React from 'react';

class RequestSidebarListItem extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    let link = `/#request/${this.props.request}`
    return (
      <li>
        <a href={link}>{ this.props.request }</a>
      </li>
    );
  }
}

RequestSidebarListItem.propTypes = {
  request : React.PropTypes.string
}


export default RequestSidebarListItem;