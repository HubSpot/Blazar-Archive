/*global config*/
import React, {Component, PropTypes} from 'react';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Loader from '../shared/Loader.jsx';

import BuildHeadline from './BuildHeadline.jsx';
import BuildDetail from './BuildDetail.jsx';
import BuildCommits from './BuildCommits.jsx';
import BuildLogNavigation from './BuildLogNavigation.jsx';
import BuildLog from './BuildLog.jsx';
import CancelBuildButton from './CancelBuildButton.jsx';
import ErrorAlert from '../shared/ErrorAlert.jsx';

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
              <ErrorAlert 
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
              <CancelBuildButton 
                triggerCancelBuild={this.props.triggerCancelBuild}
                build={this.props.build}
              />
              <BuildLogNavigation 
                navigateLogChange={this.props.navigateLogChange}
              />
            </UIGridItem>
          </UIGrid>
          <UIGrid>
            <UIGridItem size={12}>
              <BuildDetail
                build={this.props.build}
                loading={this.props.loading}
              />
            </UIGridItem>
          </UIGrid>  
        </div>
        <div className='build-body'>
          <div>  
            <BuildLog
              log={this.props.log}
              navigating={this.props.navigating}
              fetchingLog={this.props.fetchingLog}
              pageUp={this.props.pageUp}
              scrollToOffset={this.props.scrollToOffset}
              buildState={build.state}
              loading={this.props.loading}
              currentOffset={this.props.currentOffset}
              currrentOffsetLine={this.props.currrentOffsetLine}
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
  navigating: PropTypes.string,
  navigateLogChange: PropTypes.func.isRequired,
  log: PropTypes.array,
  scrollToOffset: PropTypes.number,
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
