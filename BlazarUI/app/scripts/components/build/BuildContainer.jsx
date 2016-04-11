import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import {buildIsOnDeck} from '../Helpers';
import classNames from 'classnames';

import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';

import BuildHeadline from './BuildHeadline.jsx';
import BuildLogNavigation from './BuildLogNavigation.jsx';
import BuildLog from './BuildLog.jsx';

import BuildStore from '../../stores/buildStore';
import BuildActions from '../../actions/buildActions';

const initialState = {
  data: {
    build: {},
    log: { logLines: [] }
  },
  error: false,
  loading: true
}

class BuildContainer extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'onStatusChange', 'requestNavigationChange');
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
    BuildActions.loadBuild(props.params);
  }

  tearDown() {
    this.unsubscribeFromBuild();
    BuildActions.resetBuild();
  }

  fetchNext() {
    BuildActions.fetchNext();
  }
  
  fetchPrevious() {
    BuildActions.fetchPrevious();
  }
  
  requestNavigationChange(position) {
    BuildActions.navigationChange(position);
  }

  requestPollingStateChange(change) {
    BuildActions.setLogPollingState(change);
  }

  onStatusChange(state) {
    this.setState(state);

    if (state.data.build && buildIsOnDeck(state.data.build.state)) {
      setTimeout( () => {
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

  render() {
    return (
      <PageContainer classNames='build-container'>
        <div className={this.getHeaderClasses()}>
          <UIGrid containerClassName='build-header__name-and-buttons'>
            <UIGridItem size={7}>
              <BuildHeadline 
                {...this.props}
                {...this.state}
              />
            </UIGridItem>
            <UIGridItem size={5} style={{'paddingTop': '10px'}}>
              <BuildLogNavigation 
                build={this.state.data.build}
                requestNavigationChange={this.requestNavigationChange}
                {...this.state}
              />
            </UIGridItem>
          </UIGrid>
        </div>
        <div className='build-body'>  
          <BuildLog
            build={this.state.data.build}
            log={this.state.data.log}
            fetchNext={this.fetchNext}
            fetchPrevious={this.fetchPrevious}
            positionChange={this.state.data.positionChange}
            requestPollingStateChange={this.requestPollingStateChange}
            {...this.state}
          />
        </div>
      </PageContainer>
    );
  }
}

BuildContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default BuildContainer;
