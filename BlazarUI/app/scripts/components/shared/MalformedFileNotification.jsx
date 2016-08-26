import React, {Component, PropTypes} from 'react';
import Alert from 'react-bootstrap/lib/Alert';
import json2html from 'json-to-html';
import classNames from 'classnames';

class MalformedFileNotification extends Component {

  constructor() {
    super();
    this.state = {expanded: false};

    this.toggleExpanded = this.toggleExpanded.bind(this);
  }

  toggleExpanded() {
    this.setState({
      expanded: !this.state.expanded
    });
  }

  getJSONMarkup(json) {
    return {__html: json2html(json)};
  }

  renderConfigInfo() {
    return this.props.malformedFiles.map((malformedFile, i) => {
      const {path, details} = malformedFile;

      return (
        <div key={i} className="malformed-files__file">
          <code className="malformed-files__file-name">{path}</code>
          <pre className="malformed-files__file-contents" dangerouslySetInnerHTML={this.getJSONMarkup(details)} />
        </div>
      );
    });
  }

  renderDetails() {
    const numberOfFiles = this.props.malformedFiles.length;

    return (
      <div className="malformed-files__details">
        This branch contains {numberOfFiles} malformed configuration file{numberOfFiles !== 1 && 's'}. You''ll need to correct these errors:
        {this.renderConfigInfo()}
      </div>
    );
  }

  renderAlert() {
    return (
      <Alert bsStyle="danger" className={classNames('malformed-files__alert', {expanded: this.state.expanded})}>
        One or more of your config files for this branch is malformed.
        <a onClick={this.toggleExpanded} className="pull-right">{this.state.expanded ? 'hide' : 'show'} details</a>
        {this.state.expanded && this.renderDetails()}
      </Alert>
    );
  }

  render() {
    if (this.props.loading || this.props.malformedFiles.length === 0) {
      return (
        <div />
      );
    }

    return (
      <div className="malformed-files">
        {this.renderAlert()}
      </div>
    );
  }
}

MalformedFileNotification.propTypes = {
  malformedFiles: PropTypes.array.isRequired,
  loading: PropTypes.bool.isRequired
};

export default MalformedFileNotification;
