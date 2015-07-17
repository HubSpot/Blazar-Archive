import $ from 'jQuery';

class Collection {

  constructor() {
    // holds all of our collections data
    this.data = {};
  }

  parse() {}

  fetch() {
    let promise = $.ajax({
      url: this.url(),
      type: 'GET',
      dataType: 'json'
    });

    promise.done( (resp) => {
      this.data.data = resp;
      this.parse();
    })

    return promise;

  }

}

export default Collection;
