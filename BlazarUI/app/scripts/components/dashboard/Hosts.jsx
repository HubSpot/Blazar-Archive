import React, {Component, PropTypes} from 'react';
import Collapsable from '../shared/Collapsable.jsx';
import { Link } from 'react-router';
import SectionLoader from '../shared/SectionLoader.jsx';

class Hosts extends Component {

  render() {

    if (this.props.loadingHosts) {
      return <SectionLoader align='left' />;
    }

    const hostComponents = this.props.hosts.map((host, i) => {
      const orgs = host.orgs.map((org, i) => {
        return (
          <li key={i}> 
            <Link to={org.blazarPath}>{org.name}</Link> 
          </li>
        );
      });
      
      return (
        <Collapsable
          header={host.name}
          initialToggleStateOpen={true}
          disableToggle={true}
        >  
          <ul>
            {orgs}
          </ul>
        </Collapsable> 
      );
    });
    
    return (
      <div>
        {hostComponents}
      </div>
    );
    
  }

}

Hosts.propTypes = {
  hosts: PropTypes.array,
  loadingHosts: PropTypes.bool
};

export default Hosts;
