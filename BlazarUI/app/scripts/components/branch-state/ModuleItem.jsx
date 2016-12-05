import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { connect } from 'react-redux';
import $ from 'jquery';

import Collapse from 'react-bootstrap/lib/Collapse';
import ModuleItemSummary from './ModuleItemSummary.jsx';
import ModuleBuildHistory from './module-build-history/ModuleBuildHistory.jsx';

import { shouldExpandModuleBuildHistory } from '../../selectors/moduleBuildHistorySelectors';
import { handleModuleItemClick } from '../../redux-actions/branchStateActions';

import { getCurrentBranchBuild, getCurrentModuleBuild } from '../Helpers';

const SCROLL_BUFFER = 10;
const SCROLL_DURATION = 200;

const scrollIntoViewIfNeeded = (element) => {
  const elementBottom = element.getBoundingClientRect().bottom;
  const elementHeight = element.getBoundingClientRect().height;
  const viewportHeight = document.body.clientHeight;

  const canScrollIntoView = elementHeight < viewportHeight;
  const notInView = elementBottom > viewportHeight;
  if (canScrollIntoView && notInView) {
    const amountToScroll = elementBottom - viewportHeight + SCROLL_BUFFER;
    $(document.body).animate({scrollTop: `+=${amountToScroll}`}, SCROLL_DURATION);
  }
};

const ModuleItem = ({moduleState, isExpanded, onClick}) => {
  const module = moduleState.get('module');
  const lastSuccessfulBuildNumber = moduleState.getIn(['lastSuccessfulModuleBuild', 'buildNumber']);

  let moduleItem = null;
  return (
    <li className="module-item" ref={(listItem) => {moduleItem = listItem;}}>
      <ModuleItemSummary
        module={module}
        currentModuleBuild={getCurrentModuleBuild(moduleState)}
        currentBranchBuild={getCurrentBranchBuild(moduleState)}
        isExpanded={isExpanded}
        onClick={onClick}
      />
      <Collapse in={isExpanded} onEntered={() => scrollIntoViewIfNeeded(moduleItem)}>
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
