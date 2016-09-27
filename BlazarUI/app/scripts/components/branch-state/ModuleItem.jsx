import React, { PropTypes, Component } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import ModuleBuildTabs from './ModuleBuildTabs.jsx';
import ModuleBuild from './ModuleBuild.jsx';
import { getCurrentModuleBuild, getCurrentRepoBuild } from '../Helpers';

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

  getSelectedRepoBuild() {
    const selectedRepoBuildId = this.state.selectedBuild.get('repoBuildId');
    const currentRepoBuild = getCurrentRepoBuild(this.props.moduleState);
    const lastSuccessfulRepoBuild = this.props.moduleState.get('lastSuccessfulRepoBuild');

    if (currentRepoBuild.get('id') === selectedRepoBuildId) {
      return currentRepoBuild;
    } else if (lastSuccessfulRepoBuild.get('id') === selectedRepoBuildId) {
      return lastSuccessfulRepoBuild;
    }

    return null;
  }

  render() {
    const {moduleState, onClick} = this.props;
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
          repoBuild={this.getSelectedRepoBuild()}
          onClick={onClick}
        />
      </li>
    );
  }
}

ModuleItem.propTypes = {
  moduleState: ImmutablePropTypes.map,
  onClick: PropTypes.func.isRequired
};

export default ModuleItem;
