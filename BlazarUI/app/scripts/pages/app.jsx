import React, {Component} from 'react';
import DocumentTitle from 'react-document-title';
import SidebarContainer from '../components/sidebar/SidebarContainer.jsx';
import FeedbackForm from '../components/feedback/FeedbackForm.jsx';
import ApiModal from '../components/shared/ApiModal.jsx';

class App extends Component {

  constructor() {
    this.state = {
      showApiModal: (!window.config.apiRoot)
    };
    this.closeApiModal = this.closeApiModal.bind(this);
  }

  closeApiModal() {
    this.setState({showApiModal: false});
  }

  render() {
    return (
      <DocumentTitle title='Blazar'>
        <div>
          <div className="page-wrapper">
            <SidebarContainer params={this.props.params} />
            {this.props.children}
          </div>
          <FeedbackForm />
          <ApiModal show={this.state.showApiModal} onHide={this.closeApiModal}/>
        </div>
      </DocumentTitle>
    );
  }
}

export default App;
