import React, {Component, PropTypes} from 'react';
import moment from 'moment';
import {Link} from 'react-router';

class Commit extends Component {

  renderTimestamp() {
    const {timestamp} = this.props.commitInfo;
    const formattedTime = moment(parseInt(timestamp, 10)).format('LT');

    return (
      <div className="commits-container--commit-timestamp">
        {formattedTime}
      </div>
    );
  }

  renderContent() {
    const {message, author, modified, added, removed, url} = this.props.commitInfo;
    const {name} = author;
    const filesChanged = modified.length + added.length + removed.length;

    return (
      <div className="commits-container--commit-wrapper">
        <div className="commits-container--commit-content">
          <span className="commits-container--commit-message">
            <Link to={url} target="_blank">
              {message}
            </Link>
          </span>
          <div className="commits-container--commit-details">
            <span className="commits-container--commit-author">
              {name}
            </span>
            { ' ' } changed {filesChanged} file{filesChanged === 1 ? '' : 's'}
          </div>
        </div>
      </div>
    );
  }

  renderPicture() {
    //placeholder
    return (
      <div className="commits-container--picture" />
    );
  }

  render() {
    return (
      <div className="commits-container--commit">
        {this.renderPicture()}
        {this.renderContent()}
        {this.renderTimestamp()}
      </div>
    );
  }
}

Commit.propTypes = {
  commitInfo: PropTypes.object.isRequired
}

export default Commit;
