import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { connect } from 'react-redux';

import Collapse from 'react-bootstrap/lib/Collapse';
import ModuleItemSummary from './ModuleItemSummary.jsx';
import ModuleBuildHistory from './module-build-history/ModuleBuildHistory.jsx';

import { shouldExpandModuleBuildHistory } from '../../selectors/moduleBuildHistorySelectors';
import { handleModuleItemClick } from '../../redux-actions/branchStateActions';

import { getCurrentBranchBuild, getCurrentModuleBuild } from '../Helpers';


const ModuleItem = ({moduleState, isExpanded, onClick}) => {
  const module = moduleState.get('module');
  const lastSuccessfulBuildNumber = moduleState.getIn(['lastSuccessfulModuleBuild', 'buildNumber']);

  return (
    <li className="module-item">
      <ModuleItemSummary
        module={module}
        currentModuleBuild={getCurrentModuleBuild(moduleState)}
        currentBranchBuild={getCurrentBranchBuild(moduleState)}
        isExpanded={isExpanded}
        onClick={onClick}
      />
      <Collapse in={isExpanded}>
        <div className="module-item__details">
          <ModuleBuildHistory
            moduleName={module.get('name')}
            moduleId={module.get('id')}
            lastSuccessfulBuildNumber={lastSuccessfulBuildNumber}
          />
        </div>
      </Collapse>
    </li>
  );
};

ModuleItem.propTypes = {
  moduleState: ImmutablePropTypes.map,
  isExpanded: PropTypes.bool.isRequired,
  onClick: PropTypes.func.isRequired
};

const mapStateToProps = (state, ownProps) => {
  const moduleId = ownProps.moduleState.getIn(['module', 'id']);
  return {
    isExpanded: shouldExpandModuleBuildHistory(state, {moduleId})
  };
};

const mapDispatchToProps = (dispatch, ownProps) => {
  const moduleId = ownProps.moduleState.getIn(['module', 'id']);
  return {
    onClick: () => dispatch(handleModuleItemClick(moduleId))
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(ModuleItem);
