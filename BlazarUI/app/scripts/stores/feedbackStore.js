import Reflux from 'reflux';
import FeedbackActions from '../actions/feedbackActions';


const FeedbackStore = Reflux.createStore({

  listenables: FeedbackActions,

  onSendFeedbackCompleted: function() {
    this.trigger({
      submitted: true,
      sent: true
    });
  },

  onSendFeedbackFailed: function(error) {
    this.trigger({
      submitted: true,
      sent: false,
      sendError: error
    });
  }

});

export default FeedbackStore;
