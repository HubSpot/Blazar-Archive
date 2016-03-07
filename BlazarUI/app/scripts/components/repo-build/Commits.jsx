import React, {Component, PropTypes} from 'react';
import moment from 'moment';
import classNames from 'classnames';

import Commit from './Commit.jsx';

class Commits extends Component {

  getClassNames() {
    const baseClass = 'commits-container--day-of-commits';
    const modifierSuffix = this.props.firstCommit ? '__first' : '__rest';

    return classNames([
      baseClass,
      baseClass + modifierSuffix
    ]);
  }

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
      <div className={this.getClassNames()}>
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
  timestamp: PropTypes.number.isRequired,
  firstCommit: PropTypes.bool.isRequired
}

export default Commits;
