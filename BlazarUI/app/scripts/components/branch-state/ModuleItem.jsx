import React, { PropTypes, Component } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import ModuleBuildTabs from './ModuleBuildTabs.jsx';
import ModuleBuild from './ModuleBuild.jsx';
import { getCurrentModuleBuild, getCurrentBranchBuild } from '../Helpers';

class ModuleItem extends Component {
  constructor(props) {
    super(props);
    this.state = {selectedBuild: getCurrentModuleBuild(props.moduleState)};
    this.handleSelectModuleBuild = this.handleSelectModuleBuild.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    const currentBuild = getCurrentModuleBuild(this.props.moduleState);
    const nextCurrentBuild = getCurrentModuleBuild(nextProps.moduleState);
    if (!currentBuild.equals(nextCurrentBuild)) {
      this.setState({selectedBuild: nextCurrentBuild});
    }
  }

  handleSelectModuleBuild(moduleBuild) {
    this.setState({selectedBuild: moduleBuild});
  }

  getSelectedBranchBuild() {
    const selectedRepoBuildId = this.state.selectedBuild.get('repoBuildId');
    const currentBranchBuild = getCurrentBranchBuild(this.props.moduleState);
    const lastSuccessfulBranchBuild = this.props.moduleState.get('lastSuccessfulBranchBuild');

    if (currentBranchBuild.get('id') === selectedRepoBuildId) {
      return currentBranchBuild;
    } else if (lastSuccessfulBranchBuild.get('id') === selectedRepoBuildId) {
      return lastSuccessfulBranchBuild;
    }

    return null;
  }

  render() {
    const {moduleState, isExpanded, onClick} = this.props;
    const {selectedBuild} = this.state;
    return (
      <li className="module-item" id={moduleState.getIn(['module', 'name'])}>
        <ModuleBuildTabs
          currentBuild={getCurrentModuleBuild(moduleState)}
          lastSuccessfulBuild={moduleState.get('lastSuccessfulModuleBuild')}
          selectedBuildNumber={selectedBuild.get('buildNumber')}
          onSelectModuleBuild={this.handleSelectModuleBuild}
        />
        <ModuleBuild
          module={moduleState.get('module')}
          moduleBuild={selectedBuild}
          branchBuild={this.getSelectedBranchBuild()}
          isExpanded={isExpanded}
          onClick={onClick}
        />
      </li>
    );
  }
}

ModuleItem.propTypes = {
  moduleState: ImmutablePropTypes.map,
  isExpanded: PropTypes.bool.isRequired,
  onClick: PropTypes.func.isRequired
};

export default ModuleItem;
