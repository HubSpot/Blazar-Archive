/*global config*/
import React, {Component, PropTypes} from 'react';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Loader from '../shared/Loader.jsx';

import BuildHeadline from './BuildHeadline.jsx';
import BuildDetail from './BuildDetail.jsx';

import BuildLogNavigation from './BuildLogNavigation.jsx';
import BuildLog from './BuildLog.jsx';
import AjaxErrorAlert from '../shared/AjaxErrorAlert.jsx';

class Build extends Component {

  render() {
    if (this.props.loading) {
      return (
        <Loader align='top-center' />
      );
    }

    const {
      build,
      gitInfo,
      module
    } = this.props.build;

    return (
      <div>
        <div className='build-header'>
          <UIGrid>
            <UIGridItem size={8}>
              <AjaxErrorAlert 
                error={this.props.error}
              />
              <BuildHeadline 
                moduleName={this.props.params.module}
                moduleId={this.props.params.moduleId}
                modulePath={this.props.pathname}
                buildNumber={parseInt(this.props.params.buildNumber)}
                isStarred={this.props.isStarred}
                toggleStar={this.props.toggleStar}
              />
            </UIGridItem>
            <UIGridItem size={4}>
              <BuildLogNavigation 
                changeOffsetWithNavigation={this.props.changeOffsetWithNavigation}
                buildState={build.state}
              />
            </UIGridItem>
          </UIGrid>
          <UIGrid>
            <UIGridItem size={12}>
              <BuildDetail
                build={this.props.build}
                loading={this.props.loading}
                triggerCancelBuild={this.props.triggerCancelBuild}
              />
            </UIGridItem>
          </UIGrid>  
        </div>
        <div className='build-body'>
          <div>  
            <BuildLog
              log={this.props.log}
              positionChange={this.props.positionChange}
              fetchingLog={this.props.fetchingLog}
              pageLog={this.props.pageLog}
              buildState={build.state}
              loading={this.props.loading}
              currentOffset={this.props.currentOffset}
              currrentOffsetLine={this.props.currrentOffsetLine}
              lastOffsetLine={this.props.lastOffsetLine}
            />
          </div>
        </div>
      </div>
    );
  }

}

Build.propTypes = {
  build: PropTypes.shape({
    build: PropTypes.object,
    gitInfo: PropTypes.object,
    module: PropTypes.object
  }),
  error: PropTypes.node,
  positionChange: PropTypes.node,
  changeOffsetWithNavigation: PropTypes.func.isRequired,
  log: PropTypes.array,
  fetchingLog: PropTypes.bool,
  currentOffset: PropTypes.number,
  params: PropTypes.object,
  loading: PropTypes.bool,
  toggleStar: PropTypes.func.isRequired,
  isStarred: PropTypes.bool.isRequired,
  pathname: PropTypes.string.isRequired,
  triggerCancelBuild: PropTypes.func.isRequired
};

export default Build;
