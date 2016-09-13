import React, {Component, PropTypes} from 'react';
import Immutable from 'immutable';
import CardStack from '../shared/card-stack/CardStack.jsx';

import RepoBranchCard from './RepoBranchCard.jsx';
import RepoBranchCardStackHeader from './RepoBranchCardStackHeader.jsx';
import RepoBranchCardStackZeroState from './RepoBranchCardStackZeroState.jsx';

import Loader from './Loader.jsx';

class RepoBranchCardStack extends Component {
  renderCards() {
    return this.props.starredBuilds.map((build, key) => {
      return (
        <RepoBranchCard
          key={key}
          moduleBuildsList={this.props.moduleBuildsList}
          onClick={() => this.props.onClick(key, build)}
          expanded={key === this.props.expandedCard}
          item={build}
        />
      );
    });
  }

  render() {
    if (this.props.loading) {
      return <Loader />;
    } else if (this.props.starredBuilds.size === 0) {
      return <RepoBranchCardStackZeroState />;
    }

    return (
      <CardStack condensed={true}>
        <RepoBranchCardStackHeader />
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
