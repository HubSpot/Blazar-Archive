import React from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import LastBuild from '../shared/BuildDetail.jsx';
import BuildHistoryTable from './BuildHistoryTable.jsx';
import PageHeadline from '../shared/PageHeadline.jsx';

class Module extends React.Component {

  render() {
    let latestBuild = [];

    return (
      <div>
        <PageHeader>
          <Breadcrumb />
          <PageHeadline headline={this.props.params.module} subheadline='Build History' />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <LastBuild
              build={latestBuild}
              loading={this.props.loading}
            />
            <BuildHistoryTable />
          </UIGridItem>
        </UIGrid>
      </div>
    );
  }

}

Module.propTypes = {
  loading: React.PropTypes.bool.isRequired,
  params: React.PropTypes.object.isRequired
};

export default Module;
