import React, {Component, PropTypes} from 'react';
import Alert from 'react-bootstrap/lib/Alert';

class GenericErrorMessage extends Component {

  getSeverity() {
    switch (this.props.severity) {
      case 'high':
        return 'danger';
      case 'medium':
        return 'warning';
      case 'low':
        return 'info';
      default:
        return 'danger';
    }
  }

  render() {
    if (!this.props.message) {
      return null;
    }

    return (
      <Alert bsStyle={this.getSeverity()}>
        <p>{this.props.message}</p>
      </Alert>
    );
  }
}

GenericErrorMessage.defaultProps = {
  severity: 'high'
};

GenericErrorMessage.propTypes = {
  message: PropTypes.node,
  severity: PropTypes.string
};

export default GenericErrorMessage;
