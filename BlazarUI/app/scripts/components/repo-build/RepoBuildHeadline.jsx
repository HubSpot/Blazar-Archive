import React, {Component, PropTypes} from 'react';
import Headline from '../shared/headline/Headline.jsx';
import SimpleBreadcrumbs from '../shared/SimpleBreadcrumbs.jsx';

class RepoBuildHeadline extends Component {

  renderInterProjectBuildTag() {
    const {upAndDownstreamModules} = this.props;

    if (!upAndDownstreamModules.interProjectBuildId) {
      return null;
    }

    return <span className="inter-project-tag">Inter-project build</span>;
  }

  render() {
    if (this.props.loading || !this.props.currentRepoBuild) {
      return null;
    }

    const {currentRepoBuild} = this.props;

    return (
      <div>
        <SimpleBreadcrumbs
          repo={true}
          branch={true}
          {...this.props}
        />
        <Headline className="repobuild-headline">
          <span>Build #{currentRepoBuild.buildNumber}</span>
        </Headline>
        {this.renderInterProjectBuildTag()}
      </div>
    );
  }
}

RepoBuildHeadline.propTypes = {
  params: PropTypes.object.isRequired,
  loading: PropTypes.bool.isRequired,
  branchInfo: PropTypes.object.isRequired,
  upAndDownstreamModules: PropTypes.object.isRequired,
  currentRepoBuild: PropTypes.object
};

export default RepoBuildHeadline;
