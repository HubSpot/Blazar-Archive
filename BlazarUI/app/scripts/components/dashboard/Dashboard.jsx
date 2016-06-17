/*global config*/
import React, {Component, PropTypes} from 'react';
import Immutable, { fromJS } from 'immutable'
import $ from 'jquery';
import { bindAll } from 'underscore';

import BuildStates from '../../constants/BuildStates';

import RepoBranchCardStack from '../shared/RepoBranchCardStack.jsx';

import Headline from '../shared/headline/Headline.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';

import RepoBuildActions from '../../actions/repoBuildActions';
import RepoBuildStore from '../../stores/repoBuildStore';

let initialState = {
  expandedCard: -1,
  loading: false,
  repoBuild: {},
  moduleBuildsList: []
};

class Dashboard extends Component {

  constructor(props) {
    super(props);

    this.state = initialState;
    bindAll(this, 'pollWithLatestBuild', 'onCardClick');
  }

  componentDidMount() {
    this.unsubscribeFromRepo = RepoBuildStore.listen(this.onStatusChange.bind(this));
  }

  componentWillUnmount() {
    this.unsubscribeFromRepo();

    if (this.timeout) {
      clearTimeout(this.timeout);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.state.expandedCard !== -1) {
      const starredBuilds = nextProps.starredBuilds.toJS();
      let newIndex;

      for (var i = 0; i < starredBuilds.length; i++) {
        if (starredBuilds[i].gitInfo.id === this.state.branchId) {
          newIndex = i;
        }
      }

      if (newIndex === this.state.expandedCard) {
        return;
      }

      this.setState({
        expandedCard: newIndex
      });
    }
  }

  onStatusChange(state) {
    this.setState(state);
  }

  resetSelectedCard() {
    this.setState({
      expandedCard: -1,
      build: undefined
    });

    if (this.timeout) {
      clearTimeout(this.timeout);
    }
  }

  triggerRepoBuildReload(build, branchId) {
    const repoBuildId = build.get('id');
    const buildNumber = build.get('buildNumber');

    RepoBuildActions.loadRepoBuildById(repoBuildId);
    RepoBuildActions.loadModuleBuildsById(branchId, repoBuildId, buildNumber);
  }

  getBuildByBranchId(branchId) {
    const {starredBuilds} = this.props;

    const build = this.props.starredBuilds.toJS().filter((starredBuild) => {
      return starredBuild.gitInfo.id === branchId;
    })[0];

    return fromJS(build.inProgressBuild || build.lastBuild);
  }

  pollWithLatestBuild(branchId) {
    if (branchId === undefined) {
      branchId = this.state.branchId;
    }

    const build = this.getBuildByBranchId(branchId);

    if (build.get('state') === BuildStates.IN_PROGRESS) {
      this.triggerRepoBuildReload(build, branchId);
    }

    this.timeout = setTimeout(this.pollWithLatestBuild, config.activeBuildModuleRefresh);
  }

  onCardClick(key, build) {
    if (key === this.state.expandedCard) {
      this.resetSelectedCard();
      return;
    }

    const branchId = build.get('gitInfo').get('id');

    this.setState({
      expandedCard: key,
      branchId: branchId,
      loading: true
    });

    this.triggerRepoBuildReload(this.getBuildByBranchId(branchId), branchId);
    this.pollWithLatestBuild(branchId);
  }

  render() {
    return (
      <UIGrid>
        <UIGridItem size={12} className='dashboard-unit'>
          <Headline>
            Starred Branches
          </Headline>
          <RepoBranchCardStack
            onClick={this.onCardClick}
            starredBuilds={this.props.starredBuilds}
            expandedCard={this.state.expandedCard}
            moduleBuildsList={this.state.moduleBuildsList}
            loading={this.props.loadingBuilds || this.props.loadingStars}
          />
        </UIGridItem>
      </UIGrid>
    );
  }
}

Dashboard.propTypes = {
  starredBuilds: PropTypes.instanceOf(Immutable.List),
  loadingStars: PropTypes.bool,
  loadingBuilds: PropTypes.bool,
  params: PropTypes.object
};

export default Dashboard;
