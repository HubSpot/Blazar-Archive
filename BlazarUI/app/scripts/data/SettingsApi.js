/*global config*/
import Resource from '../services/ResourceProvider';

function _parseSlackChannels(slackChannels) {
  return slackChannels.map((channel) => {
    return {
      value: channel.name,
      label: channel.name
    };
  });
}

function fetchNotifications(params, cb) {
  const notificationsPromise = new Resource({
    url: `${config.apiRoot}/instant-message/configurations/branches/${params.branchId}`,
    type: 'GET'
  }).send();

  return notificationsPromise.then((resp) => {
    cb(resp);
  });
}

function addNotification(params, channelName, cb) {
  const notification = {
    branchId: params.branchId,
    channelName: channelName,
    onFinish: false,
    onFail: false,
    onChange: false,
    onRecover: false,
    active: true
  };

  const notificationPromise = new Resource({
    url: `${config.apiRoot}/instant-message/configurations`,
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(notification)
  }).send();

  return notificationPromise.then(() => {
    this.fetchNotifications(params, cb);
  });
}

function fetchSlackChannels(cb) {
  const channelsPromise = new Resource({
    url: `${config.apiRoot}/instant-message/slack/list-channels`,
    type: 'GET'
  }).send();

  return channelsPromise.then((resp) => {
    cb(_parseSlackChannels(resp));
  });
}

function updateNotification(params, notification, cb) {
  const notificationPromise = new Resource({
    url: `${config.apiRoot}/instant-message/configurations/${notification.id}`,
    type: 'PUT',
    contentType: 'application/json',
    data: JSON.stringify(notification)
  }).send();

  return notificationPromise.then(() => {
    return this.fetchNotifications(params, cb);
  });
}

function deleteNotification(params, notificationId, cb) {
  const notificationPromise = new Resource({
    url: `${config.apiRoot}/instant-message/configurations/${notificationId}`, 
    type: 'DELETE',
    contentType: 'application/json'
  }).send();

  notificationPromise.then(() => {
    return this.fetchNotifications(params, cb);
  });
}

export default {
  fetchNotifications: fetchNotifications,
  updateNotification: updateNotification,
  fetchSlackChannels: fetchSlackChannels,
  addNotification: addNotification,
  deleteNotification: deleteNotification
};
