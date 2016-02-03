// To do: Make this specific to ajax errors
import React, {Component, PropTypes} from 'react';
import Alert from 'react-bootstrap/lib/Alert';

class AjaxErrorAlert extends Component {

  render() {    
    if (!this.props.error) {
      return null;
    }
    
    return (
      <Alert bsStyle="danger">
        <strong>Status {this.props.error.status}. Sorry, we're experiencing an error.</strong>
        <p>Response: {this.props.error.responseText}</p>
        <p>Check your console for more detail</p>
      </Alert>
    );
  }
}

AjaxErrorAlert.propTypes = {
  error: PropTypes.node
};

export default AjaxErrorAlert;
