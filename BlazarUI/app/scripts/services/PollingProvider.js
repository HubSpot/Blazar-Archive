/*global config*/
import { fromJS } from 'immutable';
import {has} from 'underscore';
import humanizeDuration from 'humanize-duration';
import Q from 'q';
import $ from 'jquery';

class PollingProvider {
  
  constructor({url, type, dataType, filter}) {
    this.url = url;
    this.type = type;
    this.dataType = dataType;
    this.shouldPoll = true;
  }

  poll(cb) {
    if (!this.shouldPoll) {
      return;
    }

    const promise = $.ajax({
      url: this.url,
      type: this.type,
      dataType: this.dataType
    });

    promise.done((resp) => {
      cb(false, resp);
    });

    promise.fail((err) => {
      cb(err, []);
    });
    
    promise.always(() => {
      setTimeout(() => {
        this.poll.call(this, cb);
      }, config.buildsRefresh)
    })
  }
  
  disconnect() {
    this.shouldPoll = false
  }  
  
}

export default PollingProvider;
