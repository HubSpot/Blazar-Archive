import React, {Component, PropTypes} from 'react';
// import HostsContainer from '../components/Hosts/BranchContainer.jsx';

class Hosts extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div>
        Hosts page here...
      </div>
    );
  }
}

Hosts.propTypes = {
  params: PropTypes.object.isRequired
};

export default Hosts;
