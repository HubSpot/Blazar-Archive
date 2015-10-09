import React, {Component, PropTypes} from 'react';
import Collapsable from '../shared/Collapsable.jsx';
import { Link } from 'react-router';
import SectionLoader from '../shared/SectionLoader.jsx';
// import DashboardHost from './DashboardHost.jsx';

class Hosts extends Component {

  render() {
    
    console.log('renaaaring: ', this.props);

    if (this.props.loadingHosts) {
      return <SectionLoader align='left' />;
    }

    const hostComponents = this.props.hosts.map((host, i) => {
      return <li key={i}>{host.name}</li>
    })
    
    return (
      <ul>
        {hostComponents}
      </ul>
    );
    
  }

}

Hosts.propTypes = {
  hosts: PropTypes.array,
  loadingHosts: PropTypes.bool
};

export default Hosts;
