import React from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import BuildHistoryTable from './BuildHistoryTable.jsx';
import PageHeadline from '../shared/PageHeadline.jsx';

class Module extends React.Component {

  render() {
    return (
      <div>
        <PageHeader>
          <Breadcrumb />
          <PageHeadline headline={this.props.params.module} subheadline='Build History' />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <BuildHistoryTable
              buildHistory={this.props.buildHistory}
              loading={this.props.loading}
            />
          </UIGridItem>
        </UIGrid>
      </div>
    );
  }

}


Module.propTypes = {
  loading: React.PropTypes.bool.isRequired,
  params: React.PropTypes.object.isRequired,
  buildHistory: React.PropTypes.array.isRequired
};

export default Module;
