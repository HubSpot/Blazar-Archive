import React, {Component} from 'react';
import ReactDOM from 'react-dom';
import {bindAll} from 'underscore';
import FormGroup from 'react-bootstrap/lib/FormGroup';
import FormControl from 'react-bootstrap/lib/FormControl';
import Button from 'react-bootstrap/lib/Button';
import Icon from '../shared/Icon.jsx';
import Alert from 'react-bootstrap/lib/Alert';
import FeedbackActions from '../../actions/feedbackActions';
import FeedbackStore from '../../stores/feedbackStore';
import { getUsernameFromCookie } from '../Helpers.js';

class FeedbackForm extends Component {

  constructor() {
    bindAll(this, 'toggleShow', 'submitFeedback', 'handleNameChange', 'handleMessageChange', 'resetForm', 'onStatusChange');
    this.state = {
      sendError: false,
      submitted: false,
      visible: false,
      nameValue: getUsernameFromCookie() || '',
      messageValue: '',
      submitDisabled: true,
      autofocus: 'name'
    };
  }

  componentDidMount() {
    this.unsubscribeFeedbackForm = FeedbackStore.listen(this.onStatusChange);
  }

  componentWillUnmount() {
    this.unsubscribeFeedbackForm();
  }

  onStatusChange(status) {
    this.setState(status);
  }

  toggleShow() {
    if (this.state.visible && this.state.submitted) {
      this.resetForm();
    }

    this.setState({
      visible: !this.state.visible,
      submitted: false
    });
  }

  resetForm() {
    this.setState({
      sendError: false,
      submitted: false,
      visible: true,
      messageValue: '',
      submitDisabled: true,
      autofocus: 'message'
    });
  }

  getContainerClassName() {
    if (this.state.visible) {
      return 'feedback-container visible';
    }

    return 'feedback-container';
  }

  handleNameChange() {
    this.setState({
      nameValue: this.nameInput.value,
      submitDisabled: !(this.nameInput.value && this.messageTextArea.value)
    });
  }

  handleMessageChange() {
    this.setState({
      messageValue: this.messageTextArea.value,
      submitDisabled: !(this.nameInput.value && this.messageTextArea.value)
    });
  }

  submitFeedback() {
    const payload = {
      username: this.state.nameValue,
      message: this.state.messageValue,
      page: window.location.href
    };

    FeedbackActions.sendFeedback(payload);
  }

  renderContent() {
    if (this.state.submitted) {
      return this.renderSubmitted();
    }

    return this.renderForm();
  }

  renderSubmitted() {
    if (this.state.sendError) {
      return (
        <Alert bsStyle="danger" className="feedback__sent" >
          <h4>Sorry, we received an error submitting feedback</h4>
          <pre>{this.state.sendError}</pre>
          <p>
            Please visit channel { ' ' }
            <span className="channel-name">
              <a href="https://hubspot.slack.com/messages/platform-support/" target="blank">#platform-support</a>
            </span> in Slack for support.</p>
        </Alert>
      );
    }

    return (
      <div className="feedback__sent">
        <h4>Thanks for helping us improve Blazar!</h4>
        <p className="big-icon"><Icon for="circle-check" /></p>
        <p>To join the conversation, hit up { ' ' }
          <span className="channel-name">
            <a href="https://hubspot.slack.com/messages/platform-support/" target="blank">#platform-support</a>
          </span> in Slack.
        </p>
        <button onClick={this.resetForm} className="btn submit-more-btn">Submit more feedback</button>
      </div>
    );
  }

  renderForm() {
    return (
      <div>
        <p>
          Running into an issue? Fill out the form below and we'll be alerted in Slack.
          <span className="text-muted"> (There's no need to include the url; we will receive it with your message.)</span>
        </p>

        <hr />
        <FormGroup>
          <FormControl
            type="text"
            placeholder="Your Name"
            ref={(ref) => {this.nameInput = ReactDOM.findDOMNode(ref);}}
            value={this.state.nameValue}
            onChange={this.handleNameChange}
            autoFocus={this.state.autofocus === 'name'}
          />
        </FormGroup>
        <FormGroup>
          <FormControl
            className="message-area"
            componentClass="textarea"
            placeholder="Message"
            ref={(ref) => {this.messageTextArea = ReactDOM.findDOMNode(ref);}}
            help=""
            value={this.state.messageValue}
            onChange={this.handleMessageChange}
            autoFocus={this.state.autofocus === 'message'}
          />
        </FormGroup>
        <Button bsStyle="info" block={true} disabled={this.state.submitDisabled} onClick={this.submitFeedback}>Submit</Button>
      </div>
    );
  }

  render() {
    return (
      <div className={this.getContainerClassName()}>
        <div className="feedback-title" onClick={this.toggleShow}>
          Get Help/Give Feedback
        </div>
        <div className="feedback__form">
          {this.renderContent()}
        </div>
      </div>
    );
  }
}

export default FeedbackForm;
