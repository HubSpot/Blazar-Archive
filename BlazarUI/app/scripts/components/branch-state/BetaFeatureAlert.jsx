import React, { PropTypes } from 'react';
import Alert from 'react-bootstrap/lib/Alert';

const BetaFeatureAlert = ({onDismiss}) => {
  return (
    <Alert bsStyle="info" onDismiss={onDismiss}>
      <h4>Beta - tell us what you think!</h4>
      <p>Hey! Your team has been selected as part of the initial rollout of the new Module-Centric builds page. This page will provide you with a more informative view of the current state of a branch and will eventually become the main page for accessing your builds in the future.</p>
      <p>We'd love to hear any feedback you have using the link at the bottom of this page.</p>
    </Alert>
  );
};

BetaFeatureAlert.propTypes = {
  onDismiss: PropTypes.func.isRequired
};

export default BetaFeatureAlert;
