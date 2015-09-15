/*global config*/
import React, {Component, PropTypes} from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import BuildHistoryTable from './BuildHistoryTable.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import SectionLoader from '../shared/SectionLoader.jsx';
import Icon from '../shared/Icon.jsx';
import Paginator from '../shared/Paginator.jsx';
import BuildButton from './BuildButton.jsx';

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
    const pagination = this.state.pagination || {};
    const paginated = Paginator.paginate(this.props.buildHistory, pagination);

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
            <Icon prefix="mega" type="octicon" name="file-directory" classNames="headline-icon" />
            <span>{this.props.params.module}</span>
            <HeadlineDetail>
              Build History
            </HeadlineDetail>
          </Headline>
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12} align='RIGHT'>
            <BuildButton moduleId={this.props.params.moduleId} />
          </UIGridItem>
          <UIGridItem size={12}>
            <BuildHistoryTable
              buildHistory={paginated.data}
              loading={this.props.loading}
            />
          </UIGridItem>
        </UIGrid>
        <div className='pagination-outer'>
          <Paginator
            hasData={this.props.buildHistory.length > 0}
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
