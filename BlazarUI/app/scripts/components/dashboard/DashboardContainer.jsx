import React, {Component} from 'react';
import {contains, pluck, isEqual} from 'underscore';
import Dashboard from './Dashboard.jsx';
import PageContainer from '../shared/PageContainer.jsx';

class DashboardContainer extends Component {

  constructor(props) {
    super(props);
  }


  render() {
    return (
      <PageContainer classNames='page-dashboard'>
        <Dashboard 
          params={this.props.params}
        />
      </PageContainer>
    );
  }
}

export default DashboardContainer;
