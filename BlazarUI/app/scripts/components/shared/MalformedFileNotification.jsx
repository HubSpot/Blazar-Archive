import React, {Component, PropTypes} from 'react';
import Alert from './Alert.jsx';
import Collapse from 'react-bootstrap/lib/Collapse';
import json2html from 'json-to-html';

class MalformedFileNotification extends Component {

  constructor(props) {
    super(props);
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

  render() {
    const {loading, malformedFiles} = this.props;

    if (loading || malformedFiles.length === 0) {
      return null;
    }

    const numberOfFiles = malformedFiles.length;
    const singular = numberOfFiles === 1;
    const alertTitle = `Malformed build configuration file${singular ? '' : 's'}`;
    const pluralizedFiles = singular ?
      'a malformed configuration file' :
      `${numberOfFiles} malformed configuration files`;

    const toggleExpandLink = (
      <a onClick={this.toggleExpanded}>
        {this.state.expanded ? 'hide' : 'show'} details
      </a>
    );

    return (
      <Alert type="danger" iconName="exclamation" titleText={alertTitle} className="malformed-files__alert">
        <p>
          Some modules in this branch are not being correctly detected by Blazar due
          to {pluralizedFiles}. {toggleExpandLink}
        </p>
        <Collapse in={this.state.expanded}>
          <div className="malformed-files__details">
            {this.renderConfigInfo()}
          </div>
        </Collapse>
      </Alert>
    );
  }
}

MalformedFileNotification.propTypes = {
  malformedFiles: PropTypes.array.isRequired,
  loading: PropTypes.bool.isRequired
};

MalformedFileNotification.defaultProps = {
  loading: false
};

export default MalformedFileNotification;
