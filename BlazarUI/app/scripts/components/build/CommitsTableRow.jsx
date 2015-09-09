import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';
import {labels, iconStatus} from '../constants';
import {bindAll, has} from 'underscore';
import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';
import {truncate, timestampFormatted} from '../Helpers';
import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';

class CommitsTableRow extends Component {

  constructor() {
    bindAll(this, 'openModal', 'closeModal', 'commitDetailModal');

    this.state = {
      showModal: false
    };
  }

  commitDetailModal() {
    // to do:
    // - get build branch in place of master
    // - expand in table??
    const githubRoot = (this.props.commit.url).split('/').slice(0,5).join('/');

    const commits = this.props.commit.modified.map( (mod, i) => {
      const link = githubRoot + '/tree/master/' + mod;
      return (
        <li key={i}> <a target='_blank' href={link}>{mod}</a> </li>
      );
    })

    return (
      <Modal show={this.state.showModal} onHide={this.closeModal.bind(this)}>
        <Modal.Header closeButton>
          <Modal.Title>Modified Files</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <ul>
            {commits}
          </ul>
        </Modal.Body>
      </Modal>
    );
  }

  closeModal() {
    this.setState({
      showModal: false
    })
  }

  openModal() {
    this.setState({
      showModal: true
    })
  }

  render() {

    const commit = this.props.commit;
    
    return (
      <tr>
        <td>
          {timestampFormatted(commit.timestamp)}
        </td>
        <td>
          {truncate(commit.message, 60, true)}
        </td>
        <td>
          {commit.author.name}
        </td> 
        <td>
          <a target='_blank' href={commit.url}>{truncate(commit.id)}</a>
        </td>
        <td onClick={this.openModal}>
          {this.commitDetailModal()}
          <Icon name='file-code-o' classNames='hoverable'/>
        </td>
      </tr>
    );
  }
}

CommitsTableRow.propTypes = {
  commit: PropTypes.object.isRequired
};

export default CommitsTableRow;
