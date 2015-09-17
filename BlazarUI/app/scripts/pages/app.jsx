import React, {Component} from 'react';
import {RouteHandler} from 'react-router';
import BuildsSidebarContainer from '../components/sidebar/BuildsSidebarContainer.jsx';
import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';
import Input from 'react-bootstrap/lib/Input';
import {bindAll} from 'underscore';

class App extends Component {

  constructor() {
    bindAll(this, 'collapseSidebar');
    this.state = {
      showModal: !window.config.apiRoot,
      sidebarCollapsed: localStorage.sidebarCollapsed === 'true'
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

  collapseSidebar() {
    localStorage.setItem('sidebarCollapsed', !this.state.sidebarCollapsed);
    this.setState({
      sidebarCollapsed: !this.state.sidebarCollapsed
    });
  }

  render() {
    return (
      <div>
        <div className="page-wrapper">
          <BuildsSidebarContainer
            collapse={this.collapseSidebar}
            isCollapsed={this.state.sidebarCollapsed} />
          <RouteHandler/>
        </div>
        {this.apiModal()}
      </div>
    );
  }
}

export default App;
