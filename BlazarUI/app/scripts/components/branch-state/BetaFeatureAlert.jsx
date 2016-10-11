import React, { PropTypes } from 'react';
import Alert from 'react-bootstrap/lib/Alert';

const BetaFeatureAlert = ({onDismiss}) => {
  return (
    <Alert bsStyle="info" onDismiss={onDismiss}>
      <h4>Beta - tell us what you think!</h4>
      <p>
        Your team has been selected as part of the initial rollout of a new
        module-centric builds page. This page combines information from the
        branch build history and build details pages to provide you with a
        more relevant view of the current state of a branch. It will eventually
        become the main page for accessing your builds.
      </p>
      <p>We'd love to hear any feedback you have using the link at the bottom of this page.</p>
    </Alert>
  );
};

BetaFeatureAlert.propTypes = {
  onDismiss: PropTypes.func.isRequired
};

export default BetaFeatureAlert;
