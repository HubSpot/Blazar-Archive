import React, {Component, PropTypes} from 'react';
import {bindAll, clone, some} from 'underscore';
import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import GenericErrorMessage from '../shared/GenericErrorMessage.jsx';

import StarStore from '../../stores/starStore';
import StarActions from '../../actions/starActions';
import ModulesHeadline from './ModulesHeadline.jsx';


let initialState = {
  modules: [],
  stars: [],
  loadingModules: true,
  loadingStars: true,
  buildTriggeringError: ''
};

class ModulesContainer extends Component {

  constructor(props) {
    super(props);
    this.state = initialState;
    
    bindAll(this, 'onStatusChange')
  }
  
  componentDidMount() {
    this.setup(this.props.params);
  }

  componentWillReceiveProps(nextprops) {
    this.tearDown()
    this.setup(nextprops.params);
    this.setState(initialState);
  }

  componentWillUnmount() {
    this.tearDown()
  }
  
  setup(params) { 
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange);
    StarActions.loadStars('moduleContainer');
  }
  
  tearDown() {
    this.unsubscribeFromStars();
  }
  
  onStatusChange(state) {
    this.setState(state);
  }

  renderSectionContent() {
    if (this.state.error) {
      return this.renderError();
    }

    else {
      return this.renderPage();
    }
  }
  
  renderError() {
    return (
      <UIGrid>
        <UIGridItem size={10}>
          <GenericErrorMessage 
            message={this.state.error}
          />
        </UIGridItem>
      </UIGrid>
    );
  }
  
  renderPage() {
    return (
      <UIGrid>
        <UIGridItem size={10}>
          <ModulesHeadline
            params={this.props.params}
            stars={this.state.stars}
            loading={false}
          />
        </UIGridItem>
      </UIGrid>
    );
  }

  render() {
    return (
      <PageContainer>
        {this.renderSectionContent()}
      </PageContainer>
    );
  }
}

ModulesContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default ModulesContainer;
