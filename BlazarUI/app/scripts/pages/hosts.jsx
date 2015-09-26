import React, {Component, PropTypes} from 'react';
import HostsContainer from '../components/hosts/HostsContainer.jsx';

class Hosts extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <HostsContainer
        params={this.props.params}
      /> 
    );
  }
}

Hosts.propTypes = {
  params: PropTypes.object.isRequired
};

export default Hosts;
