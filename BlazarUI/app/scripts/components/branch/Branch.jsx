import React from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import PageHeadline from '../shared/PageHeadline.jsx';
import BranchList from './BranchList.jsx';

class Branch extends React.Component{

  render() {
    return (
      <div>
        <PageHeader>
          <Breadcrumb />
          <PageHeadline headline={this.props.params.branch} subheadline="Branch Modules" />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <BranchList
              modules={this.props.modules}
            />
          </UIGridItem>
        </UIGrid>
      </div>
    );
  }

}

Branch.defaultProps = { loading: true };

Branch.propTypes = {
  params: React.PropTypes.object.isRequired,
  modules: React.PropTypes.array.isRequired
};

export default Branch;
