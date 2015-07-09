import React from 'react';
import RequestSidebar from './RequestSidebar.jsx';
import RequestStore from '../../stores/requestStore';
import RequestActions from '../../actions/itemActions';


class RequestSidebarContainer extends React.Component {
  
  constructor(props){
    super(props);

    this.state = {
      requests : [],
      loading: false
    };
  }

  componentDidMount() {
    this.unsubscribe = RequestStore.listen(this.onStatusChange.bind(this));
    RequestActions.loadRequests();
  }

  componentWillUnmount() {
    this.unsubscribe();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  render() {

    return (
      <RequestSidebar requests={this.state.requests} loading={this.state.loading} />
    );
  }
}

export default RequestSidebarContainer;