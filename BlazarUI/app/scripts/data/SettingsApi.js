/*global config*/
import Resource from '../services/ResourceProvider';

function fetchNotifications(params, cb) {
  const notificationsPromise = new Resource({
    url: `${config.apiRoot}/instant-message/configurations/branches/${params.branchId}`,
    type: 'GET'
  }).send();

  return notificationsPromise.then((resp) => {
    cb(resp);
  });
}

export default {
  fetchNotifications: fetchNotifications
};
