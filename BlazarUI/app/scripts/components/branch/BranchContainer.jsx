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


class BranchContainer extends Component {

  constructor() {
    this.state = {
      modules: [],
      loading: true
    };
  }

  componentDidMount() {
    this.unsubscribeFromBranch = BranchStore.listen(this.onStatusChange.bind(this));
    BranchActions.loadModules(this.props.params);
  }

  componentWillReceiveProps(nextprops) {
    BranchActions.loadModules(nextprops.params);
  }

  componentWillUnmount() {
    BranchActions.updatePollingStatus(false);
    BranchActions.loadModules(false);
    this.unsubscribeFromBranch();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  render() {
    if (this.state.loading) {
      return (
        <Loader align='top-center' />
      );
    }
    
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
