import React, {Component, PropTypes} from 'react';
import Input from 'react-bootstrap/lib/Input';
import Button from 'react-bootstrap/lib/Button';
import {bindAll} from 'underscore';

class FeedbackForm extends Component {

  constructor() {
    bindAll(this, 'toggleShow');
    this.state = {
      visable: false
    }
  }

  toggleShow() {
    this.setState({
      visable: !this.state.visable
    });
  }

  getContainerClassName() {
    if (this.state.visable) {
      return 'feedback-container visable';
    } else {
      return 'feedback-container';
    }
  }

  submitFeedback() {

  }

  render() {
    return (
      <div className={this.getContainerClassName()}>
        <div className="feedback-title" onClick={this.toggleShow}>
          Give Feedback
        </div>
        <div className="feedback-form">
          <Input
            type="text"
            label="Name"
            ref="name" />
          <Input
            className="message-area"
            type="textarea"
            label="Message"
            ref="message"
            help="The URL of the page you are on (and other data) will be submitted with this form." />
          <Button bsStyle="info" block>Submit</Button>
        </div>
      </div>
    );
  }
}

export default FeedbackForm;
