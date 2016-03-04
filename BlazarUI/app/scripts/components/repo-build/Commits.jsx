import React, {Component, PropTypes} from 'react';
import moment from 'moment';
import Commit from './Commit.jsx';

class Commits extends Component {

  renderCommits() {
    return this.props.commits.map((commit, i) => {
      return (
        <Commit
          key={i}
          commitInfo={commit} />
      );
    });
  }

  renderHeader() {
    return (
      <div class="commits-container--header">
        {moment(this.props.timestamp).format('MMM D, YYYY')}
      </div>
    );
  }

  render() {
    return (
      <div class="commits-container--day-of-commits">
        {this.renderHeader()}
        <div class="commits-container--commit-list">
          {this.renderCommits()}
        </div>
      </div>
    );
  }
}

Commits.propTypes = {
  commits: PropTypes.array.isRequired,
  timestamp: PropTypes.string.isRequired
}

export default Commits;