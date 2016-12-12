import Reflux from 'reflux';
import Feedback from '../models/Feedback';

const FeedbackActions = Reflux.createActions([
  { 'sendFeedback': {children: ['completed', 'failed']} },
  'showFeedbackForm'
]);

FeedbackActions.sendFeedback.listen(function onSendFeedback(payload) {
  const feedback = new Feedback(payload);
  feedback.submit()
    .done(this.completed)
    .fail((jqXHR, textStatus, errorThrown) => {
      this.failed(`Status ${jqXHR.status}: ${errorThrown}`);
    });
});

export default FeedbackActions;
