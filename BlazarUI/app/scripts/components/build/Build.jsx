import React, {Component, PropTypes} from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import BuildDetail from '../shared/BuildDetail.jsx';
import BuildLog from './BuildLog.jsx';
import PageHeadline from '../shared/PageHeadline.jsx';
import SectionLoader from '../shared/SectionLoader.jsx';
import Icon from '../shared/Icon.jsx';

class Build extends Component {

  getIconForState(state) {
    switch (state) {
      case 'LAUNCHING':
        return 'hourglass-start';
      case 'IN_PROGRESS':
        return 'hourglass-half';
      case 'SUCCEEDED':
      case 'FAILED':
        return 'hourglass-end';
      default:
        return 'hourglass-o';
    }
  }

  render() {

    if (this.props.loading) {
      return (
        <SectionLoader />
      );
    }

    let buildState = this.props.build.build;
    let icon = this.getIconForState(buildState.state);

    let headline = (
      <span>
        <Icon name={icon} classNames="headline-icon"></Icon>
        <span>{this.props.params.module}</span>
      </span>
    );
    let subheadline = `Build #${buildState.buildNumber}`;

    return (
      <div>
        <PageHeader>
          <Breadcrumb />
          <PageHeadline headline={headline} subheadline={subheadline} />
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
    build: PropTypes.object,
    gitInfo: PropTypes.object,
    module: PropTypes.object
  }),
  log: PropTypes.string,
  params: PropTypes.object,
  loading: PropTypes.bool
};

export default Build;
