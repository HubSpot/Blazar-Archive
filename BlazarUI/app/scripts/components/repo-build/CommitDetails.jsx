import React, {Component, PropTypes} from 'react';
import moment from 'moment';
import classNames from 'classnames';

import Icon from '../shared/Icon.jsx';
import Image from '../shared/Image.jsx';

class CommitDetails extends Component {

  getClassNames() {
    const {commitInfo} = this.props;

    return classNames(
      'commits__commit'
    );
  }

  getPictureClassNames() {
    const {commitInfo} = this.props;

    return classNames(
      'commits__picture', {
        'commits__picture--firstrow': commitInfo.firstRow,
        'commits__picture--lastrow': commitInfo.lastRow
      }
    );
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
      <a href={url} target="_blank">
        <div className="commits__commit-wrapper">
          <div className="commits__commit-content">
            <span className="commits__commit-message">
              {message}
            </span>
            <div className="commits__commit-details">
              <span className="commits__commit-author">
                {name}
              </span>
              { ' ' } changed {filesChanged} file{filesChanged === 1 ? '' : 's'}
            </div>
          </div>
        </div>
      </a>
    );
  }

  renderPicture() {
    const {commitInfo} = this.props;

    if (commitInfo.firstRow) {
      const imageUrl = `${window.config.staticRoot}/images/branch_head.png`;

      return (
        <div className={this.getPictureClassNames()}>
          <Image src={imageUrl} height={25} width={25} classNames="commits__picture-icon" />
        </div>
      );
    }

    return (
      <div className={this.getPictureClassNames()}>
        <Icon type="octicon" name="git-commit" classNames="commits__picture-icon" />
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
};

export default CommitDetails;
