import React, { Component, PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { connect } from 'react-redux';

import ModuleBuildHistoryItem from './ModuleBuildHistoryItem.jsx';
import ModuleBuildHistoryPagination from './ModuleBuildHistoryPagination.jsx';

class ModuleBuildHistory extends Component {
  renderMainContent() {
    const {moduleBuilds, moduleName, branchId} = this.props;
    if (!moduleBuilds.size) {
      return <p>No builds to display.</p>;
    }

    return (
      <ul className="historical-module-build-list">
        {moduleBuilds.map((moduleBuild) =>
          <ModuleBuildHistoryItem
            key={moduleBuild.get('id')}
            moduleBuild={moduleBuild}
            moduleName={moduleName}
            branchId={branchId}
          />
        )}
      </ul>
    );
  }

  renderPagination() {
    const {moduleName, moduleId, hasMorePages} = this.props;
    if (!hasMorePages) {
      return null;
    }

    return (
      <nav className="text-center" aria-label={`${moduleName} build history`}>
        <ModuleBuildHistoryPagination moduleId={moduleId} />
      </nav>
    );
  }

  render() {
    return (
      <div className="module-build-history">
        <h5>Recent builds</h5>
        {this.renderMainContent()}
        {this.renderPagination()}
      </div>
    );
  }
}

ModuleBuildHistory.propTypes = {
  moduleName: PropTypes.string.isRequired,
  moduleId: PropTypes.number.isRequired,
  branchId: PropTypes.number.isRequired,
  hasMorePages: PropTypes.bool,
  moduleBuilds: ImmutablePropTypes.list,
  loading: PropTypes.bool
};

const mapStateToProps = (state, ownProps) => {
  const buildHistory = state.moduleBuildHistoriesByModuleId.get(ownProps.moduleId);
  return {
    hasMorePages: buildHistory.get('totalPages') > 1,
    moduleBuilds: buildHistory.get('moduleBuilds'),
    loading: buildHistory.get('loading'),
    branchId: state.branch.getIn(['branchInfo', 'id'])
  };
};

export default connect(mapStateToProps)(ModuleBuildHistory);
