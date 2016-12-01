// To do: Make this specific to ajax errors
import React, {PropTypes} from 'react';
import Alert from 'react-bootstrap/lib/Alert';

const AjaxErrorAlert = ({error}) => {
  if (!error) {
    return null;
  }

  const {status, responseText} = error;
  if (status === 0) {
    return (
      <Alert bsStyle="danger">
        <strong>Sorry, we're having trouble getting data from Blazar.</strong>
        <p>Try refreshing the page and verify that you are connected to the internet.</p>
      </Alert>
    );
  }

  return (
    <Alert bsStyle="danger">
      <strong>Status {status}. Sorry, we're experiencing an error.</strong>
      {responseText && <p>Response: {responseText}</p>}
      <p>Check your console for more detail</p>
    </Alert>
  );
};

AjaxErrorAlert.propTypes = {
  error: PropTypes.shape({
    status: PropTypes.number,
    responseText: PropTypes.string
  })
};

export default AjaxErrorAlert;
