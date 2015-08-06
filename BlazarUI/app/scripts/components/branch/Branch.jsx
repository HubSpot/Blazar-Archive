import React, {Component, PropTypes} from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import PageHeadline from '../shared/PageHeadline.jsx';
import ModulesTable from './ModulesTable.jsx';
import SectionLoader from '../shared/SectionLoader.jsx';

class Branch extends Component{

  render() {

    if (this.props.loading) {
      return (
        <SectionLoader />
      );
    }
    let headline = (
      <span>
        <i className='mega-octicon octicon-git-branch headline-icon'></i>
        <span>{this.props.params.branch}</span>
      </span>
    );
    return (
      <div>
        <PageHeader>
          <Breadcrumb />
          <PageHeadline headline={headline} subheadline="Branch Modules" />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <ModulesTable
              modules={this.props.modules}
            />
          </UIGridItem>
        </UIGrid>
      </div>
    );
  }

}

Branch.propTypes = {
  params: PropTypes.object.isRequired,
  modules: PropTypes.array.isRequired,
  loading: PropTypes.bool.isRequired
};

export default Branch;
