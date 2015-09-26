import React, {Component, PropTypes} from 'react';
import HostContainer from '../components/host/HostContainer.jsx';

class Host extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <HostContainer
        params={this.props.params}
      /> 
    );
  }
}

Host.propTypes = {
  params: PropTypes.object.isRequired
};

export default Host;
