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
      <div className="commits-container--header">
        <span>{moment(this.props.timestamp).format('MMM D, YYYY')}</span>
      </div>
    );
  }

  render() {
    return (
      <div className="commits-container--day-of-commits">
        {this.renderHeader()}
        <div className="commits-container--commit-list">
          {this.renderCommits()}
        </div>
      </div>
    );
  }
}

Commits.propTypes = {
  commits: PropTypes.array.isRequired,
  timestamp: PropTypes.number.isRequired
}

export default Commits;
