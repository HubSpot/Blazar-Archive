import React, {Component, PropTypes} from 'react';
import {bindAll, some, clone} from 'underscore';
import {getPathname, buildIsOnDeck, getIsStarredState} from '../Helpers';
import BuildStates from '../../constants/BuildStates.js';

import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import GenericErrorMessage from '../shared/GenericErrorMessage.jsx';

import BuildHeadline from './BuildHeadline.jsx';
import BuildDetail from './BuildDetail.jsx';
import BuildLogNavigation from './BuildLogNavigation.jsx';
import BuildLog from './BuildLog.jsx';

import BuildStore from '../../stores/buildStore';
import BuildActions from '../../actions/buildActions';
import StarStore from '../../stores/starStore';
import StarActions from '../../actions/starActions';
import LocationStore from '../../stores/locationStore';

const initialState = {
  build: {
    build: {
    },
    gitInfo: {},
    // we need to fetch moduleID
    module: {
      id: -1
    }
  },
  log: {
    logLines: []
  },
  stars: [],
  loading: true,
  loadingStars: true,
  isStarred: false
}

class BuildContainer extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 
      'toggleStar', 
      'triggerCancelBuild',
      'onStatusChange',
      'requestNavigationChange'
    );
    this.haveMounted = false;
    this.state = initialState;
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
    this.tearDown()
  }
  
  setup(props) {
    this.unsubscribeFromBuild = BuildStore.listen(this.onStatusChange);
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange);
    BuildActions.loadBuild(props.params);
    // we get this detail from the sidebar on mount but need
    // to refresh it when we change to another build
    if (this.haveMounted) {
      StarActions.loadStars('buildContainer'); 
    }
  }

  tearDown() {
    this.unsubscribeFromBuild();
    this.unsubscribeFromStars();
    BuildActions.resetBuild();
  }

  fetchNext() {
    BuildActions.fetchNext();
  }
  
  fetchPrevious() {
    BuildActions.fetchPrevious();
  }

  triggerCancelBuild() {
    BuildActions.cancelBuild();
  }

  toggleStar(isStarred, starInfo) {
    StarActions.toggleStar(isStarred, starInfo);
  }
  
  requestNavigationChange(position) {
    BuildActions.navigationChange(position);
  }
  
  requestPollingStateChange(change) {
    BuildActions.setLogPollingState(change);
  }

  onStatusChange(state) {
    this.setState(state);

    if (state.build && buildIsOnDeck(state.build.build.state)) {
      setTimeout( () => {
        BuildActions.loadBuild(this.props.params);
      }, 2000);
    }
  }

  render() {
    return (
      <PageContainer>
        <div className='build-header'>
          <UIGrid containerClassName='build-header__name-and-buttons'>
            <UIGridItem size={7}>
              <BuildHeadline 
                moduleName={this.props.params.module}
                moduleId={this.state.build.module.id}
                modulePath={getPathname()}
                buildNumber={parseInt(this.props.params.buildNumber)}
                toggleStar={this.toggleStar}
                loading={this.state.loading}
                loadingStars={this.state.loadingStars || this.state.build.module.id === -1}
                isStarred={getIsStarredState(this.state.stars, this.state.build.module.id)}
              />
            </UIGridItem>
            <UIGridItem size={5} >
              <BuildLogNavigation 
                loading={this.state.loading}
                build={this.state.build}
                loading={this.state.loading}
                requestNavigationChange={this.requestNavigationChange}
              />
            </UIGridItem>
          </UIGrid>
          <UIGrid>
            <UIGridItem size={12}>
              <GenericErrorMessage 
                message={this.state.error}
              />
              <BuildDetail
                build={this.state.build}
                loading={this.state.loading}
                error={this.state.error}
                triggerCancelBuild={this.triggerCancelBuild}
              />
            </UIGridItem>
          </UIGrid>  
        </div>
        <div className='build-body'>
          <div>  
            <BuildLog
              build={this.state.build}
              log={this.state.log}
              loading={this.state.loading}
              fetchNext={this.fetchNext}
              fetchPrevious={this.fetchPrevious}
              positionChange={this.state.positionChange}
              error={this.state.error}
              requestPollingStateChange={this.requestPollingStateChange}
            />
          </div>
        </div>
      
      </PageContainer>
    );
  }
}

BuildContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default BuildContainer;
