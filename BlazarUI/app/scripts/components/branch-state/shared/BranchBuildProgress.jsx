import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';
import { buildIsInactive } from '../../Helpers';

class BranchBuildProgress extends Component {
  constructor(props) {
    super(props);
    this.state = {showProgressBar: !buildIsInactive(props.branchBuildState)};
  }

  componentWillReceiveProps(nextProps) {
    const isBuildActive = !buildIsInactive(nextProps.branchBuildState);
    if (isBuildActive) {
      this.setState({showProgressBar: true});
    } else if (this.state.showProgressBar) {
      // build has just finished. let the progress bar complete before fading out
      setTimeout(() => this.setState({showProgressBar: false}), 1200);
    }
  }

  render() {
    const {completedModuleBuildCount, totalNonSkippedModuleBuildCount} = this.props;
    const percentageComplete = completedModuleBuildCount / totalNonSkippedModuleBuildCount * 100;
    const progressBarStyle = {
      width: `${Math.min(percentageComplete, 100)}%`,
      opacity: this.state.showProgressBar ? 1 : 0
    };

    const progressClassName = classNames('branch-build-progress', {
      'branch-build-progress--inactive': !this.state.showProgressBar
    });

    return (
      <div className={progressClassName}>
        <div className="branch-build-progress-bar" style={progressBarStyle} />
      </div>
    );
  }
}

BranchBuildProgress.propTypes = {
  completedModuleBuildCount: PropTypes.number.isRequired,
  totalNonSkippedModuleBuildCount: PropTypes.number.isRequired,
  branchBuildState: PropTypes.string.isRequired
};

export default BranchBuildProgress;
