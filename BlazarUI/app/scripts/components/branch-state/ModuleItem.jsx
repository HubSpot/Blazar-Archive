import React, { PropTypes, Component } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import ModuleBuildTabs from './ModuleBuildTabs.jsx';
import ModuleBuild from './ModuleBuild.jsx';

class ModuleItem extends Component {
  constructor(props) {
    super(props);
    this.state = {selectedBuild: props.moduleState.get('lastNonSkippedBuild')};
    this.handleSelectModuleBuild = this.handleSelectModuleBuild.bind(this);
  }

  handleSelectModuleBuild(moduleBuild) {
    this.setState({selectedBuild: moduleBuild});
  }

  render() {
    const {moduleState, onClick} = this.props;
    const {selectedBuild} = this.state;
    return (
      <li className="module-item">
        <ModuleBuildTabs
          moduleState={moduleState}
          selectedBuildNumber={selectedBuild.get('buildNumber')}
          onSelectModuleBuild={this.handleSelectModuleBuild}
        />
        <ModuleBuild module={moduleState.get('module')} moduleBuild={selectedBuild} onClick={onClick} />
      </li>
    );
  }
}

ModuleItem.propTypes = {
  moduleState: ImmutablePropTypes.map,
  onClick: PropTypes.func.isRequired
};

export default ModuleItem;
