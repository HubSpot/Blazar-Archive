import React, {Component, PropTypes} from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import BuildDetail from '../shared/BuildDetail.jsx';
import BuildLog from './BuildLog.jsx';
import PageHeadline from '../shared/PageHeadline.jsx';

class Build extends Component{

  render() {
    let {buildState} = this.props.build;
    let subheadline = '';

    if (!this.props.loading) {
      subheadline = `Build #${buildState.buildNumber}`;
    }

    return (
      <div>
        <PageHeader>
          <Breadcrumb />
          <PageHeadline headline={this.props.params.module} subheadline={subheadline} />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <BuildDetail
              build={this.props.build}
              loading={this.props.loading}
            />
          </UIGridItem>
          <UIGridItem size={12}>
            <BuildLog
              log={this.props.log}
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
    buildState: PropTypes.object,
    gitInfo: PropTypes.object,
    module: PropTypes.object
  }),
  log: PropTypes.string,
  params: PropTypes.object,
  loading: PropTypes.bool
};

export default Build;
