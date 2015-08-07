import React, {Component, PropTypes} from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import PageHeadline from '../shared/PageHeadline.jsx';
import ReposTable from './ReposTable.jsx';
import SectionLoader from '../shared/SectionLoader.jsx';
import Icon from '../shared/Icon.jsx';

class org extends Component {

  render() {
    if (this.props.loading) {
      return (
        <SectionLoader />
      );
    }
    let headline = (
      <span>
        <Icon prefix="mega" type="octicon" name="organization" classNames="headline-icon" />
        <span>{this.props.params.org}</span>
      </span>
    );
    return (
      <div>
        <PageHeader>
          <Breadcrumb />
          <PageHeadline headline={headline} subheadline='Repositories' />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <ReposTable
              repos={this.props.repos}
              org={this.props.params.org}
            />
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
