/*global config*/
import React, {Component, PropTypes} from 'react';
import StarredModules from './StarredModules.jsx';

import Loader from '../shared/Loader.jsx';

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
    if (this.props.loading) {
      return (
        <Loader align='top-center' />
      );
    }

    return (
      <UIGrid>                
        <UIGridItem size={12} className='dashboard-unit'>
          <Headline>
            Starred Modules
          </Headline>
          <StarredModules 
            modulesBuildHistory={this.props.modulesBuildHistory}
            loading={this.props.loading}
          />
        </UIGridItem>
      </UIGrid>
    );
  }
}

Dashboard.propTypes = {
  loading: PropTypes.bool,
  loadingHosts: PropTypes.bool,
  modulesBuildHistory: PropTypes.array,
  params: PropTypes.object
};

export default Dashboard;
