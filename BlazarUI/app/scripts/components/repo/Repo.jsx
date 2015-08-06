import React, {Component, PropTypes} from 'react';
import Branches from './Branches.jsx';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import PageHeadline from '../shared/PageHeadline.jsx';
import SectionLoader from '../shared/SectionLoader.jsx';

class Repo extends Component {

  render() {

    if (this.props.loading) {
      return (
        <SectionLoader />
      );
    }
    let headline = (
      <span>
        <i className='mega-octicon octicon-repo headline-icon'></i>
        <span>{this.props.params.repo}</span>
      </span>
    );
    return (
      <div>
        <PageHeader>
          <Breadcrumb />
          <PageHeadline headline={headline} subheadline='Branches' />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <Branches
              branches={this.props.branches}
              loading={this.props.loading}
            />
          </UIGridItem>
        </UIGrid>
      </div>
    );
  }
}

Repo.propTypes = {
  loading: PropTypes.bool.isRequired,
  branches: PropTypes.array,
  params: PropTypes.object.isRequired
};

export default Repo;
