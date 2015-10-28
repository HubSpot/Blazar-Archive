import $ from 'jquery';

class Feedback {

  constructor(username, message, page, other) {
    this.username = username;
    this.message = message;
    this.page = page;
    this.other = other;
  }

  submit() {
    let data = {
      username: this.username || 'No Username',
      message: this.message || 'No message',
      page: this.page || 'No Page',
      other: this.other
    };
    const promise = $.ajax({
      type: 'POST',
      contentType: 'application/json',
      url: `${config.apiRoot}/feedback`,
      dataType: 'json',
      data: JSON.stringify(data)
    });

    return promise;
  }
}

export default Feedback;
