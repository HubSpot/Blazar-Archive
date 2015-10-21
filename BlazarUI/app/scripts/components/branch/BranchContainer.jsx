import React, {Component, PropTypes} from 'react';
import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import ModulesTable from './ModulesTable.jsx';
import Loader from '../shared/Loader.jsx';
import Icon from '../shared/Icon.jsx';

import BranchStore from '../../stores/branchStore';
import BranchActions from '../../actions/branchActions';

let initialState = {
  modules: [],
  loading: true
};

class BranchContainer extends Component {

  constructor() {
    this.state = initialState;
  }

  componentDidMount() {
    this.setup(this.props.params);
  }

  componentWillReceiveProps(nextprops) {
    this.tearDown();
    this.setup(nextprops.params);
    this.setState(initialState);
  }

  componentWillUnmount() {
    this.tearDown();
  }

  onStatusChange(state) {
    this.setState(state);
  }
    
  setup(params) {
    this.unsubscribeFromBranch = BranchStore.listen(this.onStatusChange.bind(this));
    BranchActions.loadModules(params);
  }
  
  tearDown() {
    BranchActions.stopPolling();
    this.unsubscribeFromBranch();
  }

  render() {
    return (
      <PageContainer>
        <UIGrid>
          <UIGridItem size={12}>
            <Headline>
              <Icon type="octicon" name="git-branch" classNames="headline-icon" />
              Branch Modules
            </Headline>
            <ModulesTable
              modules={this.state.modules}
              loading={this.state.loading}
            />
          </UIGridItem>
        </UIGrid>
      </PageContainer>
    );
  }
}


BranchContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default BranchContainer;
