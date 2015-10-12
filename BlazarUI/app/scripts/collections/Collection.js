import $ from 'jquery';

class Collection {

  parse() {}

  get() {
    return this.data;
  }

  set(data) {
    this.data = data;
  }

  fetch() {
    this.data = {};
    const promise = $.ajax({
      url: this.url(),
      type: 'GET',
      dataType: 'json'
    });

    promise.done( (resp) => {
      this.data = resp;
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
