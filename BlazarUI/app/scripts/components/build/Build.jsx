/*global config*/
import React, {Component, PropTypes} from 'react';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Loader from '../shared/Loader.jsx';

import BuildHeadline from './BuildHeadline.jsx';
import BuildDetail from './BuildDetail.jsx';

import BuildLogNavigation from './BuildLogNavigation.jsx';
import BuildLog from './BuildLog.jsx';
import GenericErrorMessage from '../shared/GenericErrorMessage.jsx';

class Build extends Component {

  getRenderContent() {
    if (this.props.loading) {
      return (
        <Loader align='top-center' />
      );
    }
    
    if (this.props.error) {
      return this.renderError();
    }
    
    else {
      return this.renderModuleAndLog();
    }
  }

  renderModuleAndLog() {
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
                loading={this.props.loading}
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
                triggerCancelBuild={this.props.triggerCancelBuild}
              />
            </UIGridItem>
          </UIGrid>  
        </div>
        <div className='build-body'>
          <div>  
            <BuildLog
              fetchingLog={this.props.fetchingLog}
              shouldPoll={this.props.shouldPoll}
              fetchStartOfLog={this.props.fetchStartOfLog}
              fetchEndOfLog={this.props.fetchEndOfLog}
              pageLog={this.props.pageLog}
              buildState={build.state}
              loading={this.props.loading}
              log={this.props.log}
            />
          </div>
        </div>
      </div>
    )
  }

  renderError() {
    return (
      <div className='build-header'>
        <UIGrid>
          <UIGridItem size={12}>
            <GenericErrorMessage
              message={this.props.error}
            />
          </UIGridItem>
        </UIGrid>
      </div>
    )
  }

  render() {
    return this.getRenderContent();
  }

}

Build.propTypes = {
  build: PropTypes.shape({
    build: PropTypes.object,
    gitInfo: PropTypes.object,
    module: PropTypes.object
  }),
  error: PropTypes.node,
  fetchStartOfLog: PropTypes.func.isRequired,
  fetchEndOfLog: PropTypes.func.isRequired,
  shouldPoll: PropTypes.func.isRequired,
  changeOffsetWithNavigation: PropTypes.func.isRequired,
  log: PropTypes.object,
  fetchingLog: PropTypes.bool,
  params: PropTypes.object,
  loading: PropTypes.bool,
  triggerCancelBuild: PropTypes.func.isRequired,
  toggleStar: PropTypes.func.isRequired,
  isStarred: PropTypes.bool.isRequired,
  pathname: PropTypes.string.isRequired
};

export default Build;
