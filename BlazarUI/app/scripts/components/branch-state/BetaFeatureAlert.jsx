import React, { PropTypes } from 'react';
import Alert from 'react-bootstrap/lib/Alert';
import FeedbackActions from '../../actions/feedbackActions';

const BetaFeatureAlert = ({onDismiss}) => {
  const feedbackFormLink = <a onClick={FeedbackActions.showFeedbackForm}>get in touch</a>;
  return (
    <Alert bsStyle="info" onDismiss={onDismiss}>
      <h4>A new perspective on your project</h4>
      <p>
        This new  page combines information previously split among several other views
        to give you a concise, live view of the current build state for this branch.
        Below <strong>you'll find the latest build information for each module</strong>,
        along with information about any pending or in-progress builds and a historical
        record of past module builds.
      </p>
      <p>
        Give it a spin! And as always if you have feedback
        please {feedbackFormLink} using the link at the bottom
        of this page.
      </p>
    </Alert>
  );
};

BetaFeatureAlert.propTypes = {
  onDismiss: PropTypes.func.isRequired
};

export default BetaFeatureAlert;
