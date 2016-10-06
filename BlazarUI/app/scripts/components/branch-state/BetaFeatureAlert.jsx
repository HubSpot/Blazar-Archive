import React, { PropTypes } from 'react';
import Alert from 'react-bootstrap/lib/Alert';

import {Link} from 'react-router';

const BetaFeatureAlert = ({branchId}) => {
  return (
    <Alert bsStyle="info">
      <h4>Beta - tell us what you think!</h4>
      <p>Hey! Your team has been selected as part of the initial rollout of the new Module-Centric builds page. This page will provide you with a more informative view of the current state of a branch and will eventually become the main page for accessing your builds in the future.</p>
      <p>We'd love to hear any feedback you have using the link at the bottom of this page.</p>
      <p className="build-history-link"><Link to={`/branches/${branchId}/builds`}>Return to branch build history page</Link></p>
    </Alert>
  );
};

BetaFeatureAlert.propTypes = {
  branchId: PropTypes.number.isRequired,
};

export default BetaFeatureAlert;
