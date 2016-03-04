import React, {Component, PropTypes} from 'react';
import moment from 'moment';

class Commit extends Component {

  renderTimestamp() {
    const {timestamp} = this.props.commitInfo;
    const formattedTime = moment(timestamp).format('LT');

    return (
      <div class="commits-container--commit-timestamp">
        {formattedTime}
      </div>
    );
  }

  renderContent() {
    const {message, author, modified, added, removed} = this.props.commitInfo;
    const {name} = author;
    const filesChanged = modified.size() + added.size() + removed.size();

    return (
      <div class="commits-container--commit-content">
        <span class="commits-container--commit-message">
          {message}
        </span>
        <div class="commits-container--commit-details">
          <span class="commits-container--commit-author">
            {name}
          </span>
          changed {filesChanged} file{filesChanged === 1 ? '' : 's'}
        </div>
      </div>
    );
  }

  renderPicture() {
    //placeholder
    return (
      <div class="commits-container--picture" />
    );
  }

  render() {
    return (
      <div class="commits-container--commit">
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