import React, {Component, PropTypes} from 'react';
import moment from 'moment';
import classNames from 'classnames';

import CommitDetails from './CommitDetails.jsx';

class CommitList extends Component {

  getClassNames() {
    const baseClass = 'commits__day-of-commits';
    const modifierSuffix = this.props.isFirstCommit ? '--first' : '--rest';

    return classNames([
      baseClass,
      baseClass + modifierSuffix
    ]);
  }

  renderCommits() {
    return this.props.commits.map((commit, i) => {
      return (
        <CommitDetails
          key={i}
          commitInfo={commit}
        />
      );
    });
  }

  renderHeader() {
    return (
      <div className="commits__header">
        <span>{moment(this.props.timestamp).format('MMM D, YYYY')}</span>
      </div>
    );
  }

  render() {
    return (
      <div className={this.getClassNames()}>
        {this.renderHeader()}
        <div className="commits__commit-list">
          {this.renderCommits()}
        </div>
      </div>
    );
  }
}

CommitList.propTypes = {
  commits: PropTypes.array.isRequired,
  timestamp: PropTypes.number.isRequired,
  isFirstCommit: PropTypes.bool.isRequired
};

export default CommitList;
