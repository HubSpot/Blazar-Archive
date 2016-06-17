import React, {Component, PropTypes} from 'react';
import Immutable from 'immutable';
import {CardStack} from 'card-stack-test';


import RepoBranchCard from './RepoBranchCard.jsx';
import RepoBranchCardStackHeader from './RepoBranchCardStackHeader.jsx';
import RepoBranchCardStackZeroState from './RepoBranchCardStackZeroState.jsx';

class RepoBranchCardStack extends Component {

  shouldComponentUpdate(nextProps) {
    return JSON.stringify(this.props.starredBuilds.toJS()) !== JSON.stringify(nextProps.starredBuilds.toJS())
      || this.props.expandedCard !== nextProps.expandedCard
      || JSON.stringify(this.props.moduleBuildsList) !== JSON.stringify(nextProps.moduleBuildsList);
  }

  renderCards() {
    const numberOfBuilds = this.props.starredBuilds.size;

    return this.props.starredBuilds.map((build, key) => {
      return (
        <RepoBranchCard
          moduleBuildsList={this.props.moduleBuildsList}
          onClick={() => this.props.onClick(key, build)}
          key={key}
          expanded={key === this.props.expandedCard}
          belowExpanded={key === this.props.expandedCard + 1 && this.props.expandedCard !== -1}
          item={build}
          loading={this.props.loading} />
      );
    });
  }

  renderHeader() {
    return (
      <RepoBranchCardStackHeader />
    );
  }

  renderRepoBranchCardStackZeroState() {
    return (
      <RepoBranchCardStackZeroState />
    );
  }

  render() {
    return (
      <CardStack
        header={this.renderHeader()}
        zeroState={this.renderRepoBranchCardStackZeroState()}
        loading={this.props.loading}>
        {this.renderCards()}
      </CardStack>
    );
  }
}

RepoBranchCardStack.propTypes = {
  onClick: PropTypes.func,
  starredBuilds: PropTypes.instanceOf(Immutable.List),
  expandedCard: PropTypes.number,
  moduleBuildsList: PropTypes.array,
  loading: PropTypes.bool
};

export default RepoBranchCardStack;
