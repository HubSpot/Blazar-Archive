import React from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import PageHeadline from '../shared/PageHeadline.jsx';

class Repo extends React.Component {

  render() {
    return (
      <div>
        <PageHeader>
          <Breadcrumb />
          <PageHeadline headline={this.props.params.repo} subheadline='Branches' />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            Branches here...
          </UIGridItem>
        </UIGrid>
      </div>
    );
  }

}


Repo.propTypes = {
  loading: React.PropTypes.bool.isRequired,
  params: React.PropTypes.object.isRequired
};

export default Repo;
