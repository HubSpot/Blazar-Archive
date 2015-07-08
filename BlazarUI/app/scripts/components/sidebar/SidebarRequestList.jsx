import React from 'react';

class SidebarRequestList extends React.Component {
  
  constructor(){
    super();
  }

  render() {
    var requests = this.props.requests.map(request => <li key={ request }>{ request }</li>),
      loading = this.props.loading ? <div className="loading-label">Loading...</div> : '';

    return (
      <div>
        { loading }
        <ul>
          { requests }
        </ul>
      </div>
    );
  }
                                     
}

SidebarRequestList.propTypes = {
  loading : React.PropTypes.bool,
  requests : React.PropTypes.array
}

export default SidebarRequestList;