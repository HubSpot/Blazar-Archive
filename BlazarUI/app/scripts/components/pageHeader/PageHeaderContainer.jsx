import React, {Component, PropTypes} from 'react';
import {clone} from 'underscore';

import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';

import ConfigStore from '../../stores/configStore';
import HostsStore from '../../stores/hostsStore';
import HostsActions from '../../actions/hostsActions';

const initialState = {
  hosts: [],
  loadingHosts: true
};

class PageHeaderContainer extends Component {
  
  constructor(props) {
    this.onStatusChange = this.onStatusChange.bind(this);
    this.state = initialState;
  }

  componentDidMount() {
    this.setup(this.props);
  }

  setup(props) {
    this.unsubscribeFromHosts = HostsStore.listen(this.onStatusChange);
    HostsActions.loadHosts()
  }

  tearDown() {
    this.unsubscribeFromHosts();
  }

  componentWillReceiveProps(nextProps) {
    this.tearDown();
    this.setup(nextProps);
    this.setState(initialState);
  }

  componentWillUnmount() {
    this.tearDown()
  }

  onStatusChange(state) {
    this.setState(state);
  }

  render() {
    return (
      <PageHeader>
        <Breadcrumb
          appRoot={ConfigStore.appRoot}
          params={this.props.params}
          hosts={this.state.hosts}
          loadingHosts={this.state.loadingHosts}
        />        
      </PageHeader>
    );
  }
}

PageHeaderContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default PageHeaderContainer;
