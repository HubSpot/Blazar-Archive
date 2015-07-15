import React from 'react';
import Build from './Build.jsx';
import PageContainer from '../layout/PageContainer.jsx';

class BuildContainer extends React.Component {

  render() {
    return (
      <PageContainer>
        <Build
          params={this.props.params}
        />
      </PageContainer>
    );
  }
}

BuildContainer.propTypes = {
  params: React.PropTypes.object.isRequired
};

export default BuildContainer;
