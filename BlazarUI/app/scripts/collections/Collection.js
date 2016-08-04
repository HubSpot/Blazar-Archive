import $ from 'jquery';
import {binarySearch} from '../utils/buildsHelpers';
import bs from 'binary-search';

class Collection {

  constructor(options = {}) {
    this.data = [];
    this.options = options;
    this.updatedTimestamp = 0;
    this.dataHasLoaded = false;
  }

  parse() {}

  get() {
    return this.data;
  }

  mergeData(incoming, current) {
    if (current === 'undefined') {
      return incoming;
    }

    current.sort((a, b) => {
      return a.module.id - b.module.id;
    });

    for (let i = 0, len = incoming.length; i < len; i++) {
      const buildIndex = binarySearch(current, incoming[i]);

      if (buildIndex > 0) {
        current[buildIndex] = incoming[i];
      }

      else {
        current.push(incoming[i]);
      }
    }

    return current;
  }

  set(data) {
    this.data = data;
  }

  fetch() {
    const promise = $.ajax({
      url: this.url(),
      type: 'GET',
      dataType: 'json'
    });

    promise.done((resp) => {
      if (this.options.mergeOnFetch) {
        this.data = this.mergeData(resp, this.data);
      }
      else {
        this.data = resp;
      }

      const timestampHeader = promise.getResponseHeader('x-last-modified-timestamp');
      if (timestampHeader) {
        this.updatedTimestamp = timestampHeader;
      }

      this.parse();
    });

    return promise;
  }

}

export default Collection;
