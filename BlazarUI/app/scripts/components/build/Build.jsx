/*global config*/
import React, {Component, PropTypes} from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import SectionLoader from '../shared/SectionLoader.jsx';

import BuildHeadline from './BuildHeadline.jsx';
import BuildDetail from '../shared/BuildDetail.jsx';
import BuildCommits from './BuildCommits.jsx';
import BuildLog from './BuildLog.jsx';

class Build extends Component {

  render() {

    if (this.props.loading) {
      return (
        <SectionLoader />
      );
    }

    const {
      build,
      gitInfo,
      module
    } = this.props.build;

    return (
      <div>
        <PageHeader>
          <Breadcrumb
            appRoot={config.appRoot}
            path={window.location.pathname}
          />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <BuildHeadline 
              moduleName={this.props.params.module}
              moduleId={this.props.params.moduleId}
              modulePath={this.props.pathname}
              buildNumber={build.buildNumber}
              isStarred={this.props.isStarred}
              toggleStar={this.props.toggleStar}
            />
            <BuildDetail
              build={this.props.build}
              loading={this.props.loading}
            />
          </UIGridItem>
          <UIGridItem size={12}>
            <BuildCommits 
              build={this.props.build}
              loading={this.props.loading}
            />
          </UIGridItem>
          <UIGridItem size={12}>
            <BuildLog
              log={this.props.log}
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
  log: PropTypes.string,
  params: PropTypes.object,
  loading: PropTypes.bool,
  toggleStar: PropTypes.func.isRequired,
  isStarred: PropTypes.bool.isRequired,
  pathname: PropTypes.string.isRequired
};

export default Build;
