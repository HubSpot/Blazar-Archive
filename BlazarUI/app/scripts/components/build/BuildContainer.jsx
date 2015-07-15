import React from 'react';
import Build from './Build.jsx';
import PageContainer from '../layout/PageContainer.jsx';

import BuildStore from '../../stores/buildStore';
import BuildActions from '../../actions/buildActions';

class BuildContainer extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      build: {
        buildNumber: null,
        startTime: null,
        endTime: null,
        commit: null,
        result: null
      },
      loading: false
    };
  }

  componentDidMount() {
    console.log('we need to fetch with these params: ', this.props.params);
    this.unsubscribe = BuildStore.listen(this.onStatusChange.bind(this));
    BuildActions.loadBuild(this.props.params);
  }

  componentWillUnmount() {
    this.unsubscribe();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  render() {
    return (
      <PageContainer>
        <Build
          build={this.state.build}
          loading={this.state.loading}
        />
      </PageContainer>
    );
  }
}

BuildContainer.propTypes = {
  params: React.PropTypes.object.isRequired
};

export default BuildContainer;
