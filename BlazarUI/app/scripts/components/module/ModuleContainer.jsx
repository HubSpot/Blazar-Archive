import React from 'react';
import Module from './Module.jsx';
import PageContainer from '../layout/PageContainer.jsx';

class ModuleContainer extends React.Component {

  constructor() {
    this.state = {
      loading: true
    };
  }

  render() {
    return (
      <PageContainer>
        <Module
          params={this.props.params}
          loading={this.state.loading}
        />
      </PageContainer>
    );
  }
}

ModuleContainer.propTypes = {
  params: React.PropTypes.object.isRequired
};

export default ModuleContainer;
