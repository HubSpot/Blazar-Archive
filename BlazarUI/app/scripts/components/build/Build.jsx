/*global config*/
import React, {Component, PropTypes} from 'react';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Loader from '../shared/Loader.jsx';

import BuildHeadline from './BuildHeadline.jsx';
import BuildDetail from './BuildDetail.jsx';
import BuildCommits from './BuildCommits.jsx';
import BuildLog from './BuildLog.jsx';
import CancelBuildButton from './CancelBuildButton.jsx';

import GenericErrorMessage from '../shared/GenericErrorMessage.jsx';

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
        <UIGrid>
          <UIGridItem size={10}>
            <BuildHeadline 
              moduleName={this.props.params.module}
              moduleId={this.props.params.moduleId}
              modulePath={this.props.pathname}
              buildNumber={parseInt(this.props.params.buildNumber)}
              isStarred={this.props.isStarred}
              toggleStar={this.props.toggleStar}
            />
          </UIGridItem>
          <UIGridItem size={2}>
            <CancelBuildButton 
              triggerCancelBuild={this.props.triggerCancelBuild}
              build={this.props.build}
            />
          </UIGridItem>
        </UIGrid>
        <UIGrid>
          <UIGridItem size={12}>
            <GenericErrorMessage
              message={this.props.error}
            />
            <BuildDetail
              build={this.props.build}
              loading={this.props.loading}
            />
          </UIGridItem>
        </UIGrid>  
        <UIGrid>  
          <UIGridItem size={12}>
            <BuildCommits 
              build={this.props.build}
              loading={this.props.loading}
            />
            <BuildLog
              log={this.props.log}
              fetchingLog={this.props.fetchingLog}
              buildState={build.state}
              loading={this.props.loading}
            />
          </UIGridItem>
        </UIGrid>
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
  log: PropTypes.string,
  fetchingLog: PropTypes.bool,
  originalParams: PropTypes.object,
  params: PropTypes.object,
  loading: PropTypes.bool,
  toggleStar: PropTypes.func.isRequired,
  isStarred: PropTypes.bool.isRequired,
  pathname: PropTypes.string.isRequired,
  triggerCancelBuild: PropTypes.func.isRequired
};

export default Build;
