import React, { Component, PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { connect } from 'react-redux';

import Loader from '../../shared/Loader.jsx';
import ModuleBuildHistoryItem from './ModuleBuildHistoryItem.jsx';
import ModuleBuildHistoryPagination from './ModuleBuildHistoryPagination.jsx';

class ModuleBuildHistory extends Component {
  renderMainContent() {
    const {moduleBuildInfos, moduleName, loading} = this.props;
    if (!moduleBuildInfos.size) {
      return loading ? <Loader /> : <p>No builds to display.</p>;
    }

    return (
      <ul className="module-build-history__list">
        {moduleBuildInfos.map((moduleBuildInfo) => {
          const moduleBuild = moduleBuildInfo.get('moduleBuild');
          const branchBuild = moduleBuildInfo.get('branchBuild');
          return (
            <ModuleBuildHistoryItem
              key={moduleBuild.get('id')}
              moduleBuild={moduleBuild}
              moduleName={moduleName}
              branchBuild={branchBuild}
            />
          );
        }

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
        <h5>Build activity</h5>
        {this.renderMainContent()}
        {this.renderPagination()}
      </div>
    );
  }
}

ModuleBuildHistory.propTypes = {
  moduleName: PropTypes.string.isRequired,
  moduleId: PropTypes.number.isRequired,
  hasMorePages: PropTypes.bool,
  moduleBuildInfos: ImmutablePropTypes.list,
  loading: PropTypes.bool
};

const mapStateToProps = (state, ownProps) => {
  const buildHistory = state.moduleBuildHistoriesByModuleId.get(ownProps.moduleId);
  return {
    hasMorePages: buildHistory.get('totalPages') > 1,
    moduleBuildInfos: buildHistory.get('moduleBuildInfos'),
    loading: buildHistory.get('loading')
  };
};

export default connect(mapStateToProps)(ModuleBuildHistory);
