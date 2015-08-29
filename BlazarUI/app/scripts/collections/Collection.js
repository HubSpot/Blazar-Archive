import $ from 'jquery';

class Collection {

  parse() {}

  fetch() {
    this.data = {};
    let promise = $.ajax({
      url: this.url(),
      type: 'GET',
      dataType: 'json'
    });

    promise.done( (resp) => {
      this.data = resp;
      this.parse();
    });

    return promise;

  }

}

export default Collection;
