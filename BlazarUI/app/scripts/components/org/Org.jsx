import React, {Component, PropTypes} from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import PageHeadline from '../shared/PageHeadline.jsx';

class org extends Component {

  render() {
    return (
      <div>
        <PageHeader>
          <Breadcrumb />
          <PageHeadline headline={this.props.params.org} subheadline='Repositories' />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            
          </UIGridItem>
        </UIGrid>
      </div>
    );
  }

}


org.propTypes = {
  loading: PropTypes.bool.isRequired,
  params: PropTypes.object.isRequired
};

export default org;
