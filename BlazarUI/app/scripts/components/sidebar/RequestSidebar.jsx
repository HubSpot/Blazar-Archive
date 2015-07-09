import React from 'react';
import RequestSidebarListItem from './RequestSidebarListItem.jsx'

class RequestSidebar extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    let requests = this.props.requests.map( (request, i) =>
      <RequestSidebarListItem key={i} request={request} />
    );
    let loading = this.props.loading ? <div>Loading Requests...</div> : '';

    return (
      <div className='sidebar'>
        <h2>Projects</h2>
        {loading}
        {requests}
      </div>
    );
  }
}

RequestSidebar.propTypes = {
  loading : React.PropTypes.bool,
  requests : React.PropTypes.array
}


export default RequestSidebar;