/*global config*/
import { fromJS } from 'immutable';
import {has} from 'underscore';
import humanizeDuration from 'humanize-duration';
import $ from 'jquery';
import Resource from './ResourceProvider';

class PollingProvider {
  
  constructor({url, type, dataType, filter}) {
    this.url = url;
    this.type = type;
    this.dataType = dataType;
    this.shouldPoll = true;
    
    this.resource = new Resource({
      url: this.url,
      type: this.type,
      dataType: this.dataType,
      type: 'GET'
    });
  }

  poll(cb) {
    if (!this.shouldPoll) {
      return;
    }

    const promise = this.resource.send();

    promise.done((resp) => {
      if (cb) {
        cb(false, resp);  
      }  
    });

    promise.fail((err) => {
      console.warn(err);
      if (cb) {
        cb(err, null);  
      }
    });
    
    promise.always(() => {
      setTimeout(() => {
        this.poll.call(this, cb);
      }, config.buildsRefresh);
    });
  }
  
  disconnect() {
    this.shouldPoll = false;
  }  
  
}

export default PollingProvider;
