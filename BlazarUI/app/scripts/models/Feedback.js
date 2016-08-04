import $ from 'jquery';

class Feedback {

  constructor(payload) {
    const {
      username,
      message,
      page,
      other
    } = payload;

    this.username = username;
    this.message = message;
    this.page = page;
    this.other = other;
  }

  submit() {
    const data = {
      username: this.username || 'No Username',
      message: this.message || 'No message',
      page: this.page || 'No Page',
      other: this.other
    };

    return $.ajax({
      type: 'POST',
      contentType: 'application/json',
      url: `${window.config.apiRoot}/user-feedback`,
      dataType: 'json',
      data: JSON.stringify(data)
    });
  }
}

export default Feedback;
