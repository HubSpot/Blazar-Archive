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

function fetchSlackChannels(cb) {
  const channelsPromise = new Resource({
    url: `${config.apiRoot}/instant-message/slack/list-channels`,
    type: 'GET'
  }).send();

  return channelsPromise.then((resp) => {
    cb(resp);
  });
}

function updateNotification(params, notification, cb) {
  console.log(notification);
  const notificationsPromise = new Resource({
    url: `${config.apiRoot}/instant-message/configurations/${notification.id}`,
    type: 'PUT',
    contentType: 'application/json',
    data: JSON.stringify(notification)
  }).send();

  return notificationsPromise.then(() => {
    return this.fetchNotifications(params, cb);
  })
}

export default {
  fetchNotifications: fetchNotifications,
  updateNotification: updateNotification,
  fetchSlackChannels: fetchSlackChannels
};
