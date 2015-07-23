import React, {Component, PropTypes} from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import PageHeadline from '../shared/PageHeadline.jsx';
import Branches from './Branches.jsx';

class Repo extends Component {

  render() {
    return (
      <div>
        <PageHeader>
          <Breadcrumb />
          <PageHeadline headline={this.props.params.repo} subheadline='Branches' />
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
