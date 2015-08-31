/*global config*/
import React, {Component, PropTypes} from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
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

    return (
      <div>
        <PageHeader>
          <Breadcrumb
            appRoot={config.appRoot}
            path={window.location.pathname}
          />
          <Headline>
            <Icon prefix="mega" type="octicon" name="organization" classNames="headline-icon" />
            <span>{this.props.params.org}</span>
            <HeadlineDetail>
              Repositories
            </HeadlineDetail>
          </Headline>
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <ReposTable
              repos={this.props.repos}
              org={this.props.params.org}
              host={this.props.params.url}
            />
          </UIGridItem>
        </UIGrid>
      </div>
    );
  }

}

org.propTypes = {
  loading: PropTypes.bool.isRequired,
  params: PropTypes.object.isRequired,
  repos: PropTypes.array.isRequired
};

export default org;
