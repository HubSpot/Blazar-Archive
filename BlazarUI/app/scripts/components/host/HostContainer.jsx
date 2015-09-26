import React, {Component, PropTypes} from 'react';
import PageContainer from '../shared/PageContainer.jsx';

class HostContainer extends Component {

  render() {
    return (
      <PageContainer>
        Placeholder: Host Detail Here
      </PageContainer>
    );
  }
}

HostContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default HostContainer;
