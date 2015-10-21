import React, {Component, PropTypes} from 'react';
import {clone} from 'underscore';

import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumbs from './Breadcrumbs.jsx';

import ConfigStore from '../../stores/configStore';

class HeaderContainer extends Component {

  render() {
    return (
      <PageHeader>
        <Breadcrumbs
          appRoot={ConfigStore.appRoot}
          params={this.props.params}
        />        
      </PageHeader>
    );
  }
}

HeaderContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default HeaderContainer;
