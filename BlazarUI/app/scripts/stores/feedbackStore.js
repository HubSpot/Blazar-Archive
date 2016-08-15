import Reflux from 'reflux';
import FeedbackActions from '../actions/feedbackActions';


const FeedbackStore = Reflux.createStore({

  listenables: FeedbackActions,

  onSendFeedbackCompleted() {
    this.trigger({
      submitted: true,
      sent: true
    });
  },

  onSendFeedbackFailed(error) {
    this.trigger({
      submitted: true,
      sent: false,
      sendError: error
    });
  }

});

export default FeedbackStore;
