/*global config*/
import React, {Component, PropTypes} from 'react';
import Immutable from 'immutable'
import StarredBranches from './StarredBranches.jsx';

import Headline from '../shared/headline/Headline.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';


class Dashboard extends Component {

  render() {
    return (
      <UIGrid>                
        <UIGridItem size={12} className='dashboard-unit'>
          <Headline>
            Starred Branches
          </Headline>
           <StarredBranches
            starredBuilds={this.props.starredBuilds}
            loadingStars={this.props.loadingStars}
            loadingBuilds={this.props.loadingBuilds}
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
