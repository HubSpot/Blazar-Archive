/*global config*/
import React, {Component, PropTypes} from 'react';
import {some} from 'underscore';

import {getIsStarredState} from '../Helpers.js';
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
import Star from '../shared/Star.jsx';

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
            params={this.props.originalParams}
          />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={10}>
            <Headline>
              <Star
                className='icon-roomy'
                isStarred={getIsStarredState(this.props.stars, this.props.params.moduleId)}
                toggleStar={this.props.toggleStar} 
                modulePath={this.props.pathname}
                moduleName={this.props.params.module}
                moduleId={this.props.params.moduleId}
                updateWithState={true}
              />
              {this.props.params.module} 
              <HeadlineDetail>
                Build History
              </HeadlineDetail>
            </Headline>
          </UIGridItem>           
          <UIGridItem size={2} align='RIGHT'>
            <BuildButton triggerBuild={this.props.triggerBuild} buildTriggering={this.props.buildTriggering} />
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
  buildHistory: PropTypes.array.isRequired,
  pathname: PropTypes.string.isRequired,
  stars: PropTypes.array.isRequired,
  triggerBuild: PropTypes.func.isRequired,
  buildTriggering: PropTypes.bool,
  toggleStar: PropTypes.func.isRequired
};

export default Module;
