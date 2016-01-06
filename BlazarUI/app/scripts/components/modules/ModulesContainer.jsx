import React, {Component, PropTypes} from 'react';
import {bindAll, clone, some} from 'underscore';
import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import GenericErrorMessage from '../shared/GenericErrorMessage.jsx';


let initialState = {

};

class ModulesContainer extends Component {

  constructor(props) {
    super(props);
    this.state = initialState;
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
        <UIGridItem size={12}>
          Modules here...
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
