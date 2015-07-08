import React from 'react';
import RequestList from './SidebarRequestList.jsx';
import RequestStore from '../../stores/requestStore';
import RequestActions from '../../actions/itemActions';


class Sidebar extends React.Component {
  
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
      <div className='sidebar'>
        <p>Sidebar</p>
        <RequestList { ...this.state } />
      </div>
    );
  }
}

export default Sidebar;