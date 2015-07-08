import Reflux from 'reflux';
import ItemActions from '../actions/itemActions';

var RequestStore = Reflux.createStore({

  init() {
    this.requests = [];

    this.listenTo(ItemActions.loadRequests, this.loadRequests);
    this.listenTo(ItemActions.loadRequestsSuccess, this.loadRequestsSuccess);
    this.listenTo(ItemActions.loadRequestsError, this.loadRequestsError);
  },

  loadRequests() {
    this.trigger({ 
      loading: true
    });
  },

  loadRequestsSuccess(requests) {
    this.requests = requests;

    this.trigger({ 
      requests : this.requests,
      loading: false
    });
  },

  loadRequetsError(error) {
    this.trigger({ 
      error : error,
      loading: false
    });
  }

});

export default RequestStore;