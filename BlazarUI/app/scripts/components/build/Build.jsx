/*global config*/
import React, {Component, PropTypes} from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import BuildDetail from '../shared/BuildDetail.jsx';
import BuildLog from './BuildLog.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import SectionLoader from '../shared/SectionLoader.jsx';
import Icon from '../shared/Icon.jsx';
import { BUILD_ICONS } from '../constants';

class Build extends Component {

  render() {

    if (this.props.loading) {
      return (
        <SectionLoader />
      );
    }

    let buildState = this.props.build.build;

    return (
      <div>
        <PageHeader>
          <Breadcrumb
            appRoot={config.appRoot}
            path={window.location.pathname}
          />
          <Headline>
            <Icon name={BUILD_ICONS[buildState.state]} classNames="headline-icon"></Icon>
            <span>{this.props.params.module}</span>
            <HeadlineDetail>
              Build # {buildState.buildNumber}
            </HeadlineDetail>
          </Headline>
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
