import React from 'react';
import { connect } from 'react-redux';
import Pagination from 'react-bootstrap/lib/Pagination';
import Icon from '../../shared/Icon.jsx';
import { selectPage } from '../../../redux-actions/moduleBuildHistoryActions';

const mapStateToProps = (state, ownProps) => {
  const buildHistory = state.moduleBuildHistoriesByModuleId.get(ownProps.moduleId);
  return {
    activePage: buildHistory.get('page'),
    next: <span>Next <Icon name="chevron-right" /></span>,
    prev: <span><Icon name="chevron-left" /> Prev</span>,
    ellipsis: false,
    items: buildHistory.get('totalPages'),
    maxButtons: 5,
    className: 'module-build-history__pagination'
  };
};

const mapDispatchToProps = (dispatch, ownProps) => ({
  onSelect: (page) => dispatch(selectPage(ownProps.moduleId, page))
});

export default connect(mapStateToProps, mapDispatchToProps)(Pagination);
