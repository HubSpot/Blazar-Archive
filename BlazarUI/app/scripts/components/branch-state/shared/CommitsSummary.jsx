import React, { Component, PropTypes } from 'react';
import ReactDOM from 'react-dom';
import ImmutablePropTypes from 'react-immutable-proptypes';
import Immutable from 'immutable';
import classNames from 'classnames';
import {bindAll} from 'underscore';

import Overlay from 'react-bootstrap/lib/Overlay';
import Popover from 'react-bootstrap/lib/Popover';

import Icon from '../../shared/Icon.jsx';
import CommitsSummaryPopover from './CommitsSummaryPopover.jsx';
import { getCompareUrl } from '../../../utils/commitInfoHelpers';

// add some delay to allow scrolling across commits
// in a table without triggering any popovers
const DELAY = 200;

class CommitsSummary extends Component {
  constructor(props) {
    super(props);
    this.state = {show: false};
    bindAll(this, 'delayedShow', 'delayedHide');
  }

  showPopover() {
    this.setState({show: true});
  }

  hidePopover() {
    this.setState({show: false});
  }

  delayedShow() {
    clearTimeout(this.hideDelay);
    this.hideDelay = null;

    if (!this.showDelay) {
      this.showDelay = setTimeout(() => {
        this.showDelay = null;
        this.showPopover();
      }, DELAY);
    }
  }

  delayedHide() {
    clearTimeout(this.showDelay);
    this.showDelay = null;

    if (!this.hideDelay) {
      this.hideDelay = setTimeout(() => {
        this.hideDelay = null;
        this.hidePopover();
      }, DELAY);
    }
  }

  render() {
    const {commitInfo, className, popoverPlacement, buildId} = this.props;
    const currentCommit = commitInfo.get('current');
    const newCommits = commitInfo.get('newCommits');

    const hasNewCommits = !newCommits.isEmpty();
    const commitList = hasNewCommits ?
      newCommits.sort((a, b) => b.get('timestamp') - a.get('timestamp')) :
      Immutable.List.of(currentCommit);

    const compareCommitsUrl = getCompareUrl(commitInfo);

    let text;
    if (hasNewCommits) {
      text = (commitList.size === 1) ? '1 commit' : `${commitList.size} commits`;
    } else {
      text = 'latest commit';
    }

    return (
      <span>
        <span
          className={classNames('commits-summary', className)}
          ref={(ref) => {this.popoverTrigger = ref;}}
          onMouseEnter={this.delayedShow}
          onMouseLeave={this.delayedHide}
        >
          <Icon type="octicon" name="git-commit" /> {text}
        </span>

        <Overlay
          show={this.state.show}
          placement={popoverPlacement}
          target={() => ReactDOM.findDOMNode(this.popoverTrigger)}
        >
          <Popover
            ref={(ref) => {this.popover = ref;}}
            onMouseEnter={this.delayedShow}
            onMouseLeave={this.delayedHide}
            id={`commit-summary-popover-${buildId}`}
          >
            <CommitsSummaryPopover
              commitList={commitList}
              compareCommitsUrl={compareCommitsUrl}
            />
          </Popover>
        </Overlay>
      </span>
    );
  }
}

CommitsSummary.propTypes = {
  commitInfo: ImmutablePropTypes.mapContains({
    current: ImmutablePropTypes.map,
    newCommits: ImmutablePropTypes.list
  }),
  className: PropTypes.string,
  popoverPlacement: PropTypes.string,
  buildId: PropTypes.number.isRequired
};

CommitsSummary.defaultProps = {
  popoverPlacement: 'bottom'
};

export default CommitsSummary;
