import React, {Component, PropTypes} from 'react';
import Immutable, { fromJS } from 'immutable';
import {bindAll} from 'underscore';

import BuildStates from '../../constants/BuildStates';

import CardStack from '../shared/CardStack.jsx';
import RepoBranchCard from '../shared/RepoBranchCard.jsx';
import RepoBranchCardStackHeader from '../shared/RepoBranchCardStackHeader.jsx';
import RepoBranchCardStackZeroState from '../shared/RepoBranchCardStackZeroState.jsx';

import Headline from '../shared/headline/Headline.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';

import RepoBuildActions from '../../actions/repoBuildActions';
import RepoBuildStore from '../../stores/repoBuildStore';

class Dashboard extends Component {

  constructor(props) {
    super(props);

    this.state = {
      expandedCard: -1,
      loading: false,
      repoBuild: {},
      moduleBuildsList: []
    };

    bindAll(this, 'onStatusChange', 'pollWithLatestBuild');
  }

  componentDidMount() {
    this.unsubscribeFromRepo = RepoBuildStore.listen(this.onStatusChange);
  }

  componentWillReceiveProps(nextProps) {
    if (this.state.expandedCard !== -1) {
      const starredBuilds = nextProps.starredBuilds.toJS();
      let newIndex;

      for (let i = 0; i < starredBuilds.length; i++) {
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

  componentWillUnmount() {
    this.unsubscribeFromRepo();

    if (this.timeout) {
      clearTimeout(this.timeout);
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

    this.timeout = setTimeout(this.pollWithLatestBuild, window.config.activeBuildModuleRefresh);
  }

  onCardClick(key, build) {
    if (key === this.state.expandedCard) {
      this.resetSelectedCard();
      return;
    }

    const branchId = build.get('gitInfo').get('id');

    this.setState({
      expandedCard: key,
      branchId,
      loading: true
    });

    this.triggerRepoBuildReload(this.getBuildByBranchId(branchId), branchId);
    this.pollWithLatestBuild(branchId);
  }

  renderCards() {
    const numberOfBuilds = this.props.starredBuilds.size;

    return this.props.starredBuilds.map((build, key) => {
      return (
        <RepoBranchCard
          {...this.state}
          onClick={() => this.onCardClick(key, build)}
          key={key}
          expanded={key === this.state.expandedCard}
          belowExpanded={key === this.state.expandedCard + 1 && this.state.expandedCard !== -1}
          first={key === 0}
          last={key === numberOfBuilds - 1}
          item={build}
          loading={this.props.loadingBuilds || this.props.loadingStars || this.state.loading}
        />
      );
    });
  }

  render() {
    return (
      <UIGrid>
        <UIGridItem size={12} className="dashboard-unit">
          <Headline>
            Starred Branches
          </Headline>
          <CardStack
            header={<RepoBranchCardStackHeader />}
            zeroState={<RepoBranchCardStackZeroState />}
            loading={this.props.loadingBuilds || this.props.loadingStars}>
            {this.renderCards()}
          </CardStack>
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
