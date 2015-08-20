import React, {Component, PropTypes} from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import BuildHistoryTable from './BuildHistoryTable.jsx';
import PageHeadline from '../shared/PageHeadline.jsx';
import SectionLoader from '../shared/SectionLoader.jsx';
import Icon from '../shared/Icon.jsx';
import Paginator from '../shared/Paginator.jsx';

class Module extends Component {

  constructor() {
    this.state = {
      pagination: {
        perPage: 15,
        page: 0
      }
    };
  }

  onPage(page) {
    this.state.pagination.page = page;
    this.forceUpdate();
  }

  render() {
    let pagination = this.state.pagination || {};
    let paginated = Paginator.paginate(this.props.buildHistory, pagination);

    if (this.props.loading) {
      return (
        <SectionLoader />
      );
    }
    let moduleName = this.props.params.module.substring(0, this.props.params.module.lastIndexOf('_'));
    let headline = (
      <span>
        <Icon prefix="mega" type="octicon" name="file-directory" classNames="headline-icon" />
        <span>{moduleName}</span>
      </span>
    );
    return (
      <div>
        <PageHeader>
          <Breadcrumb />
          <PageHeadline headline={headline} subheadline='Build History' />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <BuildHistoryTable
              buildHistory={paginated.data}
              loading={this.props.loading}
            />
          </UIGridItem>
        </UIGrid>
        <div className='pagination-outer'>
          <Paginator
            className='pagination'
            ellipsesClassName='disabled'
            selectedClassName='active'
            page={paginated.page}
            pages={paginated.amount}
            beginPages={1}
            endPages={1}
            showPrevNext={true}
            onSelect={this.onPage.bind(this)}>
          </Paginator>
        </div>
      </div>
    );
  }
}

Module.propTypes = {
  loading: PropTypes.bool.isRequired,
  params: PropTypes.object.isRequired,
  buildHistory: PropTypes.array.isRequired
};

export default Module;
