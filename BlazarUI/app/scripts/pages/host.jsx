import React, {Component, PropTypes} from 'react';
// import HostContainer from '../components/Host/BranchContainer.jsx';

class Host extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div>
        Host page here...
      </div>
    );
  }
}

Host.propTypes = {
  params: PropTypes.object.isRequired
};

export default Host;
