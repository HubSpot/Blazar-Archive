import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import Input from 'react-bootstrap/lib/Input';
import Button from 'react-bootstrap/lib/Button';
import Icon from '../shared/Icon.jsx';
import Alert from 'react-bootstrap/lib/Alert';
import Feedback from '../../models/Feedback';
import FeedbackActions from '../../actions/feedbackActions';
import FeedbackStore from '../../stores/feedbackStore';

class FeedbackForm extends Component {

  constructor() {
    bindAll(this, 'toggleShow', 'submitFeedback', 'handleNameChange', 'handleMessageChange', 'resetForm', 'onStatusChange');
    this.state = {
      sendError: false,
      submitted: false,
      visible: false,
      nameValue: '',
      messageValue: '',
      submitDisabled: true,
      submitted: false,
      autofocus: 'name'
    }
  }
  
  componentDidMount() {
    this.unsubscribeFeedbackForm = FeedbackStore.listen(this.onStatusChange)
  }
  
  componentWillUnmount() {
    this.unsubscribeFeedbackForm()
  }
  
  onStatusChange(status) {
    this.setState(status);
  }

  toggleShow() {
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
    } else {
      return 'feedback-container';
    }
  }

  handleNameChange() {
    this.setState({
      nameValue: this.refs.name.getValue(),
      submitDisabled: !(this.refs.name.getValue() && this.refs.message.getValue())
    });
  }

  handleMessageChange() {
    this.setState({
      messageValue: this.refs.message.getValue(),
      submitDisabled: !(this.refs.name.getValue() && this.refs.message.getValue())
    });
  }

  submitFeedback() {
    const payload = {
      username: this.state.nameValue,
      message: this.state.messageValue,
      page: window.location.href
    }

    FeedbackActions.sendFeedback(payload);
  }

  renderContent() {
    if (this.state.submitted) {
      return this.renderSubmitted();
    }
    else {
      return this.renderForm();
    }
  }

  renderSubmitted() {
    if (this.state.sendError) {
      return (
        <Alert bsStyle="danger" className="feedback__sent" >
          <h4>Sorry, we received an error submitting feedback</h4>
          <pre>{this.state.sendError}</pre>
          <p>Please visit channel  { ' ' }
            <span className="channel-name">
              <a href="https://hubspot.slack.com/messages/blazar/" target="blank">#blazar</a>
            </span> in Slack for support.</p>
        </Alert>
      )
    }

    return (
      <div className="feedback__sent">
        <h4>Thanks for helping us improve Blazar!</h4>
        <p className="big-icon"><Icon for="circle-check"/></p>
        <p>To join the conversation, hit up { ' ' }
          <span className="channel-name">
            <a href="https://hubspot.slack.com/messages/blazar/" target="blank">#blazar</a>
          </span> in Slack.
        </p>
        <button onClick={this.resetForm} className='btn submit-more-btn'>Submit more feedback</button>
      </div>
    );
  }

  renderForm() {
    return (
      <div>
        <p>
          Running into an issue? Fill out the form below and we'll be alerted in Slack channel <a href="https://hubspot.slack.com/messages/blazar/" target="blank">#blazar</a>.
          <span className='text-muted'> (No need to include the url, we will receive it with your message)</span>
        </p>
        
        <hr/>
        <Input
          type="text"
          placeholder="Your Name"
          ref="name"
          value={this.state.nameValue}
          onChange={this.handleNameChange} 
          autoFocus={this.state.autofocus === 'name'}
        />
        <Input
          className="message-area"
          type="textarea"
          placeholder="Message"
          ref="message"
          help=""
          value={this.state.messageValue}
          onChange={this.handleMessageChange} 
          autoFocus={this.state.autofocus === 'message'}
        />
        <Button bsStyle="info" block disabled={this.state.submitDisabled} onClick={this.submitFeedback}>Submit</Button>
      </div>
    );
  }

  render() {
    return (
      <div className={this.getContainerClassName()}>
        <div className="feedback-title" onClick={this.toggleShow}>
          Give Feedback
        </div>
        <div className="feedback__form">
          {this.renderContent()}
        </div>
      </div>
    );
  }
}

export default FeedbackForm;