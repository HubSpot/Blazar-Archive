import React, { Component, PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { connect } from 'react-redux';
import $ from 'jquery';

import Collapse from 'react-bootstrap/lib/Collapse';
import ModuleItemSummary from './ModuleItemSummary.jsx';
import ModuleBuildHistory from './module-build-history/ModuleBuildHistory.jsx';

import { isModuleItemSelected, getLastRequestStartTime, hasPreviouslyLoadedData } from '../../selectors/moduleBuildHistorySelectors';
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

const DELAY_BEFORE_SHOWING_LOADING_ICON = 400;

class ModuleItem extends Component {
  constructor(props) {
    super(props);
    this.state = {isExpanded: props.isSelected};
  }

  componentWillReceiveProps(nextProps) {
    const {isSelected, hasPreviouslyLoaded, lastRequestStartTime} = nextProps;
    clearTimeout(this.expandTimeoutId);

    if (!isSelected) {
      this.setState({isExpanded: false});
    } else if (hasPreviouslyLoaded || this.state.isExpanded) {
      this.setState({isExpanded: true});
    } else {
      // initial fetch was started but data has not been loaded yet
      // adding a delay smooths out the expansion of the module item details
      const timeSinceInitialFetch = Date.now() - lastRequestStartTime;
      const initialFetchTakingToLong = timeSinceInitialFetch > DELAY_BEFORE_SHOWING_LOADING_ICON;

      if (initialFetchTakingToLong) {
        this.setState({isExpanded: true});
      } else {
        this.setState({isExpanded: false});
        this.expandTimeoutId = setTimeout(
          () => this.setState({isExpanded: true}),
          DELAY_BEFORE_SHOWING_LOADING_ICON - timeSinceInitialFetch
        );
      }
    }
  }

  componentDidUpdate(prevProps, prevState) {
    if (prevState.isExpanded && !prevProps.hasPreviouslyLoaded) {
      scrollIntoViewIfNeeded(this.moduleItem);
    }
  }

  render() {
    const {moduleState, onClick} = this.props;
    const {isExpanded} = this.state;
    const module = moduleState.get('module');
    const lastSuccessfulBuildNumber = moduleState.getIn(['lastSuccessfulModuleBuild', 'buildNumber']);

    return (
      <li className="module-item" ref={(listItem) => {this.moduleItem = listItem;}}>
        <ModuleItemSummary
          module={module}
          currentModuleBuild={getCurrentModuleBuild(moduleState)}
          currentBranchBuild={getCurrentBranchBuild(moduleState)}
          isExpanded={isExpanded}
          onClick={onClick}
        />
        <Collapse in={isExpanded} onEntered={() => scrollIntoViewIfNeeded(this.moduleItem)}>
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
  }
}

ModuleItem.propTypes = {
  moduleState: ImmutablePropTypes.map,
  isSelected: PropTypes.bool.isRequired,
  lastRequestStartTime: PropTypes.number,
  hasPreviouslyLoaded: PropTypes.bool,
  onClick: PropTypes.func.isRequired
};

const mapStateToProps = (state, ownProps) => {
  const moduleId = ownProps.moduleState.getIn(['module', 'id']);
  return {
    isSelected: isModuleItemSelected(state, {moduleId}),
    lastRequestStartTime: getLastRequestStartTime(state, {moduleId}),
    hasPreviouslyLoaded: hasPreviouslyLoadedData(state, {moduleId})
  };
};

const mapDispatchToProps = (dispatch, ownProps) => {
  const moduleId = ownProps.moduleState.getIn(['module', 'id']);
  return {
    onClick: () => dispatch(handleModuleItemClick(moduleId))
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(ModuleItem);
