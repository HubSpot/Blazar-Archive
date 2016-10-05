import React, { PropTypes } from 'react';
import Alert from 'react-bootstrap/lib/Alert';

import {Link} from 'react-router';

const BetaFeatureAlert = ({branchId}) => {
  return (
    <Alert bsStyle="info">
      <h4>Beta - tell us what you think!</h4>
      <p>Hey! You're seeing this new page because you’re  part of the Blazar beta group. We'd love to hear any feedback you have using the link at the bottom of this page.</p>
      <p><Link to={`/branches/${branchId}/builds`}>Return to the previous design</Link></p>
    </Alert>
  );
};

BetaFeatureAlert.propTypes = {
  branchId: PropTypes.number.isRequired,
};

export default BetaFeatureAlert;
