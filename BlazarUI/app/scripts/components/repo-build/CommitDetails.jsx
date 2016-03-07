import React, {Component, PropTypes} from 'react';
import moment from 'moment';
import classNames from 'classnames';

import Icon from '../shared/Icon.jsx';
import Image from '../shared/Image.jsx';

import {Link} from 'react-router';

class CommitDetails extends Component {

  getClassNames() {
    const {commitInfo} = this.props;
    const rowSuffix = commitInfo.oddRow ? '--odd' : '--even';

    return classNames([
      'commits__commit',
      'commits__commit' + rowSuffix
    ]);
  }

  getPictureClassNames() {
    const {commitInfo} = this.props;
    const rowSuffix = commitInfo.oddRow ? '--odd' : '--even';
    const firstRowSuffix = commitInfo.firstRow ? '--firstrow' : '';
    const lastRowSuffix = commitInfo.lastRow ? '--lastrow' : '';

    return classNames([
      'commits__picture',
      'commits__picture' + rowSuffix,
      'commits__picture' + firstRowSuffix,
      'commits__picture' + lastRowSuffix
    ]);
  }

  renderTimestamp() {
    const {timestamp} = this.props.commitInfo;
    const formattedTime = moment(parseInt(timestamp, 10)).format('LT');

    return (
      <div className="commits__commit-timestamp">
        {formattedTime}
      </div>
    );
  }

  renderContent() {
    const {message, author, modified, added, removed, url} = this.props.commitInfo;
    const {name} = author;
    const filesChanged = modified.length + added.length + removed.length;

    return (
      <div className="commits__commit-wrapper">
        <div className="commits__commit-content">
          <span className="commits__commit-message">
            <Link to={url} target="_blank">
              {message}
            </Link>
          </span>
          <div className="commits__commit-details">
            <span className="commits__commit-author">
              {name}
            </span>
            { ' ' } changed {filesChanged} file{filesChanged === 1 ? '' : 's'}
          </div>
        </div>
      </div>
    );
  }

  renderPicture() {
    const {commitInfo} = this.props;
    const iconName = commitInfo.firstRow ? 'git-branch' : 'git-commit';
    const imageUrl = `${window.config.staticRoot}/images/branch_head.png`;

    if (commitInfo.firstRow) {
      return (
        <div className="commits__git-branch-icon">
          <Image src={imageUrl} height="25" width="25" classNames="commits__picture-icon" />
        </div>
      );
    }

    return (
      <div className={this.getPictureClassNames()}>
        <Icon type="octicon" name={iconName} classNames="commits__picture-icon" />
      </div>
    );
  }

  render() {
    return (
      <div className={this.getClassNames()}>
        {this.renderPicture()}
        {this.renderContent()}
        {this.renderTimestamp()}
      </div>
    );
  }
}

CommitDetails.propTypes = {
  commitInfo: PropTypes.object.isRequired
}

export default CommitDetails;
