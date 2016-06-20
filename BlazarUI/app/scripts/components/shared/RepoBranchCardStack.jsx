import React, {Component, PropTypes} from 'react';
import Immutable from 'immutable';
import {CardStack} from 'card-stack-test';

import RepoBranchCard from './RepoBranchCard.jsx';
import RepoBranchCardStackHeader from './RepoBranchCardStackHeader.jsx';
import RepoBranchCardStackZeroState from './RepoBranchCardStackZeroState.jsx';

import Loader from './Loader.jsx';

class RepoBranchCardStack extends Component {

  shouldComponentUpdate(nextProps) {
    return JSON.stringify(this.props.starredBuilds.toJS()) !== JSON.stringify(nextProps.starredBuilds.toJS())
      || this.props.expandedCard !== nextProps.expandedCard
      || JSON.stringify(this.props.moduleBuildsList) !== JSON.stringify(nextProps.moduleBuildsList);
  }

  renderCards() {
    return this.props.starredBuilds.map((build, key) => {
      return (
        <RepoBranchCard
          key={key}
          moduleBuildsList={this.props.moduleBuildsList}
          onClick={() => this.props.onClick(key, build)}
          expanded={key === this.props.expandedCard}
          belowExpanded={key === this.props.expandedCard + 1 && this.props.expandedCard !== -1}
          item={build}
        />
      );
    });
  }

  render() {
    if (this.props.loading) {
      return <Loader />;
    }

    else if (this.props.starredBuilds.size === 0) {
      return <RepoBranchCardStackZeroState />;
    }

    return (
      <CardStack
        header={<RepoBranchCardStackHeader />}
      >
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
