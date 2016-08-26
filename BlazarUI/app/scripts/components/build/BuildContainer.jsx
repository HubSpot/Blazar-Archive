import React, {Component, PropTypes} from 'react';
import {connect} from 'react-redux';
import {bindAll} from 'underscore';
import {buildIsOnDeck} from '../Helpers';
import classNames from 'classnames';

import {
  TailerProvider,
  BlazarLogTailer
} from 'singularityui-tailer/src/components';

import {
  blazarSetApiRoot
} from 'singularityui-tailer/src/actions';

import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';

import BuildHeadline from './BuildHeadline.jsx';
import BuildLogNavigation from './BuildLogNavigation.jsx';

import BuildStore from '../../stores/buildStore';
import BuildActions from '../../actions/buildActions';

import BranchStore from '../../stores/branchStore';
import BranchActions from '../../actions/branchActions';

const initialState = {
  data: {
    build: {},
    log: { logLines: [] }
  },
  branchInfo: {},
  error: false,
  loading: true,
  loadingBranchInfo: true
};

class BuildContainer extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'onStatusChange', 'requestNavigationChange');
    this.haveMounted = false;
    this.state = initialState;
  }

  componentWillMount() {
    this.props.blazarSetApiRoot();
  }

  componentDidMount() {
    this.haveMounted = true;
    this.setup(this.props);
  }

  componentWillReceiveProps(nextProps) {
    this.tearDown();
    this.setup(nextProps);
    this.setState(initialState);
  }

  componentWillUnmount() {
    this.tearDown();
  }

  setup(props) {
    this.unsubscribeFromBuild = BuildStore.listen(this.onStatusChange);
    this.unsubscribeFromBranch = BranchStore.listen(this.onStatusChange);
    BuildActions.loadBuild(props.params);
    BranchActions.loadBranchInfo(props.params);
  }

  tearDown() {
    this.unsubscribeFromBuild();
    this.unsubscribeFromBranch();
    BuildActions.resetBuild();
  }

  fetchNext() {
    // BuildActions.fetchNext();
  }

  fetchPrevious() {
    // BuildActions.fetchPrevious();
  }

  requestNavigationChange(position) {
    BuildActions.navigationChange(position);
  }

  requestPollingStateChange(change) {
    // BuildActions.setLogPollingState(change);
  }

  onStatusChange(state) {
    this.setState(state);

    if (state.data && state.data.build && buildIsOnDeck(state.data.build.state)) {
      setTimeout(() => {
        BuildActions.loadBuild(this.props.params);
      }, 2000);
    }
  }

  getHeaderClasses() {
    const buildState = this.state.data.build.state;

    if (buildState) {
      return classNames([
        'build-header',
        `alert-state-${buildState}`
      ]);
    }
    return null;
  }

  buildDocumentTitle() {
    const {branchInfo, data} = this.state;
    const {moduleName, buildNumber} = this.props.params;

    const titlePrefix = `${moduleName} #${(data.build.buildNumber || buildNumber)}`;
    const titleInfo = branchInfo ? ' | ' + `${branchInfo.repository} - ${branchInfo.branch}` : '';

    return titlePrefix + titleInfo;
  }

  render() {
    let maybeTailer;

    const { build } = this.state.data;
    if (build.hasOwnProperty('id')) {
      const tailerId = `${build.id}/log`;
      maybeTailer = (
        <BlazarLogTailer
          tailerId={tailerId}
          buildId={`${build.id}`}
        />
      );
    }

    return (
      <PageContainer documentTitle={this.buildDocumentTitle()} classNames="build-container">
        <div className={this.getHeaderClasses()}>
          <UIGrid containerClassName="build-header__name-and-buttons">
            <UIGridItem size={8}>
              <BuildHeadline
                {...this.props}
                {...this.state}
              />
            </UIGridItem>
            <UIGridItem size={4} style={{'paddingTop': '15px'}}>
              <BuildLogNavigation
                build={this.state.data.build}
                requestNavigationChange={this.requestNavigationChange}
                {...this.state}
              />
            </UIGridItem>
          </UIGrid>
        </div>
        <TailerProvider getTailerState={(state) => state.tailer}>
          <div className="build-body" heap-ignore="1">
            {maybeTailer}
          </div>
        </TailerProvider>
      </PageContainer>
    );
  }
}

const { apiRootOverride } = localStorage;

const mapDispatchToProps = (dispatch) => ({
  blazarSetApiRoot: () => dispatch(blazarSetApiRoot(
    `${apiRootOverride || ''}`
  ))
});


BuildContainer.propTypes = {
  blazarSetApiRoot: PropTypes.func.isRequired,
  params: PropTypes.object.isRequired
};

export default connect(
  null,
  mapDispatchToProps
)(BuildContainer);
