import Reflux from 'reflux';
import Feedback from '../models/Feedback';

const FeedbackActions = Reflux.createActions([
  { 'sendFeedback': {children: ['completed', 'failed']} }
]);

FeedbackActions.sendFeedback.listen((payload) => {
  const feedback = new Feedback(payload);
  // to do: implement reflux catch method, waiting on an issue response.
  feedback.submit()
    .done(() => {
      this.completed();
    })
    .fail((jqXHR, textStatus, errorThrown) => {
      this.failed(`Status ${jqXHR.status}: ${errorThrown}`);
    });
});

export default FeedbackActions;
