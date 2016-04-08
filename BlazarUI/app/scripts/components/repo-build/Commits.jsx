import React, {Component, PropTypes} from 'react';
import moment from 'moment';

import CommitList from './CommitList.jsx';

class Commits extends Component {

  splitCommitsIntoDays() {
    let commitMap = {}; // keyed by day

    let sortedCommits = this.props.commits.sort((a, b) => {
      return b.timestamp - a.timestamp;
    });

    if (!this.props.showCommits) {
      sortedCommits = sortedCommits.slice(0, 1);
    }

    sortedCommits.map((commit, i) => {
      const day = moment(parseInt(commit.timestamp, 10)).format('M-DD-YY');
      let entry;

      commit.oddRow = i % 2 === 0;
      commit.firstRow = i === 0;
      commit.lastRow = i === sortedCommits.length - 1;

      if (commitMap[day] !== undefined) {
        entry = commitMap[day];
        entry.push(commit);
      }

      else {
        entry = [commit];
      }

      commitMap[day] = entry;
    });

    return commitMap;
  }

  buildCompareLink() {
    const {currentCommit, previousCommit} = this.props;

    return previousCommit.url.replace('/commit/', '/compare/') + '...' + currentCommit.id;
  }

  renderSummaryText() {
    const {anyNewCommits, showCommits, commits, previousCommit} = this.props;
    let msg;

    if (!anyNewCommits) {
      msg = 'No new commits in this build. Showing most recent commit';
    }

    else if (!showCommits) {
      msg = `Showing 1 of ${commits.length} new commits in this build`;
    }

    else {
      msg = `Showing ${commits.length} of ${commits.length} new commit${commits.length === 1 ? '' : 's'} in this build`;
    }

    let compareNode;

    if (previousCommit !== undefined) {
      compareNode = (
        <a href={this.buildCompareLink()}>compare</a>
      );
    }

    return (
      <span>
        {msg}
        <span className='commits__compare-link'>
          {compareNode}
        </span>
      </span>
    )
  }

  renderSummary() {
    return (
      <div className="commits__summary">
        {this.renderSummaryText()}
      </div>
    );
  }

  renderCommits() {
    const commitMap = this.splitCommitsIntoDays();

    const children = Object.keys(commitMap).map((day, i) => {
      const commitList = commitMap[day];
      const timestamp = parseInt(commitList[0].timestamp, 10);
      const isFirstCommit = i === 0;

      return (
        <CommitList
          key={i}
          isFirstCommit={isFirstCommit}
          commits={commitList}
          timestamp={timestamp} />
      );
    });

    return (
      <div className="commits">
        {children}
      </div>
    );
  }

  renderShowText() {
    const {commits, showCommits} = this.props;

    if (commits.length === 1) {
      return (
        <span />
      );
    }

    return (
      <a className="commits__view-all" onClick={this.props.flipShowCommits}>
        show {showCommits ? 'fewer' : 'more'}
      </a>
    );
  }

  render() {
    return (
      <div className="commits__wrapper">
        {this.renderSummary()}
        {this.renderCommits()}
        {this.renderShowText()}
      </div>
    );
  }
}

Commits.propTypes = {
  currentCommit: PropTypes.object.isRequired,
  previousCommit: PropTypes.object,
  commits: PropTypes.array.isRequired,
  showCommits: PropTypes.bool.isRequired,
  anyNewCommits: PropTypes.bool.isRequired,
  flipShowCommits: PropTypes.func.isRequired
};

export default Commits;
