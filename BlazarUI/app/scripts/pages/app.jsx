import React, {Component} from 'react';
import SidebarContainer from '../components/sidebar/SidebarContainer.jsx';
import FeedbackForm from '../components/feedback/FeedbackForm.jsx';
import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';
import Input from 'react-bootstrap/lib/Input';

class App extends Component {

  constructor() {
    this.state = {
      showModal: (!window.config.apiRoot)
    };
  }

  apiModal() {
    return (
      <Modal show={this.state.showModal} onHide={this.close.bind(this)}>
        <Modal.Header closeButton>
          <Modal.Title>API Root Override</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Input
            type='text'
            placeholder='https://path.to-api.com/v1/api'
            label='Enter API root URL to use:'
            id='apiInput'
          />
        </Modal.Body>
        <Modal.Footer>
          <Button onClick={this.close.bind(this)}>Close</Button>
          <Button bsStyle='primary' onClick={this.setUrl.bind(this)}>Set</Button>
        </Modal.Footer>
      </Modal>
    );
  }

  setUrl() {
    const url = document.getElementById('apiInput').value;
    localStorage.apiRootOverride = url;
    this.close();
    location.reload();
  }

  open() {
    this.state.showModal = true;
    this.forceUpdate();
  }

  close() {
    this.state.showModal = false;
    this.forceUpdate();
  }

  render() {
    const modal = this.apiModal();

    return (
      <div>
        <div className="page-wrapper">
          <SidebarContainer params={this.props.params} />
          {this.props.children}
        </div>
        <FeedbackForm/>
        {modal}
      </div>
    );
  }
}

export default App;
