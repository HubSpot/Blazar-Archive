/*global config*/
import React, {Component, PropTypes} from 'react';
import {some} from 'underscore';

import {getIsStarredState} from '../Helpers.js';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import BuildHistoryTable from './BuildHistoryTable.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Loader from '../shared/Loader.jsx';
import Icon from '../shared/Icon.jsx';
import BuildButton from './BuildButton.jsx';
import Star from '../shared/Star.jsx';

class Module extends Component {

  render() {
    if (this.props.loading) {
      return (
        <Loader align='top-center' />
      );
    }

    return (
      <UIGrid>
        <UIGridItem size={10}>
          <Headline>
            <Star
              className='icon-roomy'
              isStarred={getIsStarredState(this.props.stars, this.props.params.moduleId)}
              toggleStar={this.props.toggleStar} 
              modulePath={this.props.pathname}
              moduleName={this.props.params.module}
              moduleId={this.props.params.moduleId}
              updateWithState={true}
            />
            {this.props.params.module} 
            <HeadlineDetail>
              Build History
            </HeadlineDetail>
          </Headline>
        </UIGridItem>           
        <UIGridItem size={2} align='RIGHT'>
          <BuildButton triggerBuild={this.props.triggerBuild} />
        </UIGridItem>
        <UIGridItem size={12}>
          <BuildHistoryTable
            buildHistory={this.props.buildHistory}
            loading={this.props.loading}
          />
        </UIGridItem>
      </UIGrid>
    );
  }
}

Module.propTypes = {
  loading: PropTypes.bool.isRequired,
  params: PropTypes.object.isRequired,
  buildHistory: PropTypes.array.isRequired,
  pathname: PropTypes.string.isRequired,
  stars: PropTypes.array.isRequired,
  triggerBuild: PropTypes.func.isRequired,
  toggleStar: PropTypes.func.isRequired
};

export default Module;
