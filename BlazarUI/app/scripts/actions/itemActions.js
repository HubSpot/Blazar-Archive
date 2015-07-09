import Reflux from 'reflux';

var RequestActions = Reflux.createActions([
  'loadRequests',
  'loadRequestsSuccess',
  'loadRequestsError'
]);

RequestActions.loadRequests.preEmit = function(data){
  // we will put api call/ async stuff here
  // temporarily using setTimeout for faking async behaviour
  setTimeout(function(){
    var Requests = ['Australia', 'NewZealand', 'Singapore', 'Tonga'];
    RequestActions.loadRequestsSuccess(Requests);

    // on error
    // RequestActions.loadRequestsError('an error occured');
  },500);
};

export default RequestActions;