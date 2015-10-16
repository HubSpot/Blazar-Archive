/*global config*/
import React, {Component, PropTypes} from 'react';
import StarredModules from './StarredModules.jsx';

import Headline from '../shared/headline/Headline.jsx';
import PageHeader from '../shared/PageHeader.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import MutedMessage from '../shared/MutedMessage.jsx';
import Icon from '../shared/Icon.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';
import Helpers from '../ComponentHelpers';


class Dashboard extends Component {

  render() {
    return (
      <UIGrid>                
        <UIGridItem size={12} className='dashboard-unit'>
          <Headline>
            Starred Modules
          </Headline>
          <StarredModules 
            modulesBuildHistory={this.props.modulesBuildHistory}
            loadingStars={this.props.loadingStars}
            loadingModulesBuildHistory={this.props.loadingModulesBuildHistory}
            loadingStars={this.props.loadingStars}
          />
        </UIGridItem>
      </UIGrid>
    );
  }
}

Dashboard.propTypes = {
  loadingModulesBuildHistory: PropTypes.bool,
  loadingStars: PropTypes.bool,
  loadingHosts: PropTypes.bool,
  modulesBuildHistory: PropTypes.array,
  params: PropTypes.object
};

export default Dashboard;
