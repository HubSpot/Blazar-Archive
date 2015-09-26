import React, {Component, PropTypes} from 'react';
import PageContainer from '../shared/PageContainer.jsx';

class HostsContainer extends Component {

  render() {
    return (
      <PageContainer>
        Placeholder: Hosts List Here
      </PageContainer>
    );
  }
}

HostsContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default HostsContainer;
