// To do: Make this specific to ajax errors
import React, {Component, PropTypes} from 'react';
import Alert from 'react-bootstrap/lib/Alert';

class ErrorAlert extends Component {

  render() {    
    if (!this.props.error) {
      return null;
    }
    
    return (
      <Alert bsStyle="danger">
        <h4>Status {this.props.error.status}. Sorry, we're experiencing an error.</h4>
        <p>{this.props.error.responseText}</p>
      </Alert>
    );
  }
}

ErrorAlert.propTypes = {
  error: PropTypes.node
};

export default ErrorAlert;
