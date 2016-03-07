import React, {Component, PropTypes} from 'react';
import moment from 'moment';
import Commits from './Commits.jsx';

class CommitsContainer extends Component {

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

    return Object.keys(commitMap).map((day, i) => {
      const commitList = commitMap[day];
      const timestamp = parseInt(commitList[0].timestamp, 10);

      return (
        <Commits
          key={i}
          commits={commitList}
          timestamp={timestamp} />
      );
    });
  }

  render() {
    return (
      <div className="commits-container">
        {this.renderCommits()}
      </div>
    );
  }
}

CommitsContainer.propTypes = {
  commits: PropTypes.array.isRequired
};

export default CommitsContainer;
