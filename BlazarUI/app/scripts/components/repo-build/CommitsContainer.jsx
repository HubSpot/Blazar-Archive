import React, {Component, PropTypes} from 'react';
import moment from 'moment';
import Commits from './Commits.jsx';

class CommitsContainer extends Component {

  splitCommitsIntoDays() {
    let commitMap; // keyed by day
    this.props.commits.map((commit) => {
      const day = moment(commit.timestamp).format('M-DD-YY');
      let entry;

      if (day in commitMap) {
        entry = [commit];
      }

      else {
        entry = commitMap[day];
      }

      commitMap[day] = entry;
    });

    return commitMap;
  }

  renderCommits() {
    const commitMap = this.splitCommitsIntoDays();

    return Object.keys(commitMap).map((day) => {
      const commitList = commitMap.get(day);
      const timestamp = commitList[0].timestamp;

      return (
        <Commits
          commits={commitList}
          timestamp={timestamp} />
      );
    });
  }

  render() {
    return (
      <div class="commits-container">
        {this.renderCommits()}
      </div>
    );
  }
}

CommitsContainer.propTypes = {
  commits: PropTypes.object.isRequired // this is "newCommits" piece of the object
};

export default CommitsContainer;