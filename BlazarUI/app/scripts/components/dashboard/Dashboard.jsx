/*global config*/
import React, {Component, PropTypes} from 'react';
import Immutable, { fromJS } from 'immutable'
import $ from 'jquery';
import { bindAll, where } from 'underscore';

import BuildStates from '../../constants/BuildStates';

import CardStack from '../shared/CardStack.jsx';
import RepoBranchCard from '../shared/RepoBranchCard.jsx';
import RepoBranchCardStackHeader from '../shared/RepoBranchCardStackHeader.jsx';

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
  }

  componentDidMount() {
    this.unsubscribeFromRepo = RepoBuildStore.listen(this.onStatusChange.bind(this));
  }

  componentWillUnmount() {
    this.unsubscribeFromRepo();
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
        expandedCard: i
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
  }

  pollWithLatestBuild() {
    const {branchId} = this.state;
    const build = fromJS(this.props.starredBuilds.toJS().filter((starredBuild) => {
      return starredBuild.gitInfo.id === branchId;
    })[0]);
    const buildToUse = build.has('inProgressBuild') ? build.get('inProgressBuild') : build.get('lastBuild');

    if (buildToUse.get('state') === BuildStates.IN_PROGRESS) {
      const repoBuildId = buildToUse.get('id');
      const buildNumber = buildToUse.get('buildNumber');

      RepoBuildActions.loadRepoBuildById(repoBuildId);
      RepoBuildActions.loadModuleBuildsById(branchId, repoBuildId, buildNumber);
    }

    setTimeout(this.pollWithLatestBuild.bind(this), 1000);
  }

  onCardClick(key, build) {
    if (key === this.state.expandedCard) {
      this.resetSelectedCard();
      return;
    }

    this.setState({
      expandedCard: key,
      branchId: build.get('gitInfo').get('id'),
      loading: true
    });

    const newBuild = fromJS(this.props.starredBuilds.toJS().filter((starredBuild) => {
      return starredBuild.gitInfo.id === build.get('gitInfo').get('id');
    })[0]);

    const buildToUse = newBuild.has('inProgressBuild') ? newBuild.get('inProgressBuild') : newBuild.get('lastBuild');
    const repoBuildId = buildToUse.get('id');
    const buildNumber = buildToUse.get('buildNumber');
    const branchId = newBuild.get('gitInfo').get('id');
    RepoBuildActions.loadRepoBuildById(repoBuildId);
    RepoBuildActions.loadModuleBuildsById(branchId, repoBuildId, buildNumber);

    setTimeout(this.pollWithLatestBuild.bind(this), 5000);
  }

  renderCards() {
    const numberOfBuilds = this.props.starredBuilds.size;

    return this.props.starredBuilds.map((build, key) => {
      return (
        <RepoBranchCard
          {...this.state}
          onClick={this.onCardClick.bind(this, key, build)}
          key={key}
          expanded={key === this.state.expandedCard} 
          belowExpanded={key === this.state.expandedCard + 1 && this.state.expandedCard !== -1}
          first={key === 0}
          last={key === numberOfBuilds - 1}
          item={build}
          loading={this.props.loadingBuilds || this.props.loadingStars || this.state.loading} />
      );
    })
  }

  renderHeader() {
    return (
      <RepoBranchCardStackHeader />
    );
  }

  render() {
    return (
      <UIGrid>                
        <UIGridItem size={12} className='dashboard-unit'>
          <Headline>
            Starred Branches
          </Headline>
          <CardStack
            header={this.renderHeader()}
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
