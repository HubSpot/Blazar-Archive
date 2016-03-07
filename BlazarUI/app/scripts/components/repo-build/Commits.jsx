import React, {Component, PropTypes} from 'react';
import moment from 'moment';

import CommitList from './CommitList.jsx';

class Commits extends Component {

  splitCommitsIntoDays() {
    let commitMap = {}; // keyed by day
    this.props.commits.sort((a, b) => {
        return b.timestamp - a.timestamp;
      }).map((commit) => {
      const day = moment(parseInt(commit.timestamp, 10)).format('M-DD-YY');
      let entry;

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

  renderCommits() {
    const commitMap = this.splitCommitsIntoDays();
    let topBorder = true;

    return Object.keys(commitMap).map((day, i) => {
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
  }

  render() {
    return (
      <div className="commits">
        {this.renderCommits()}
      </div>
    );
  }
}

Commits.propTypes = {
  commits: PropTypes.array.isRequired
};

export default Commits;
