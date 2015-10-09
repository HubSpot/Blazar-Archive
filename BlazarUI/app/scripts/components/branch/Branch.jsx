/*global config*/
import React, {Component, PropTypes} from 'react';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import ModulesTable from './ModulesTable.jsx';
import SectionLoader from '../shared/SectionLoader.jsx';
import Icon from '../shared/Icon.jsx';

class Branch extends Component{

  render() {

    if (this.props.loading) {
      return (
        <SectionLoader />
      );
    }

    return (
      <UIGrid>
        <UIGridItem size={12}>
          <Headline>
            <Icon type="octicon" name="git-branch" classNames="headline-icon" />
            Branch Modules
          </Headline>
          <ModulesTable
            modules={this.props.modules}
          />
        </UIGridItem>
      </UIGrid>
    );
  }

}

Branch.propTypes = {
  params: PropTypes.object.isRequired,
  modules: PropTypes.array.isRequired,
  loading: PropTypes.bool.isRequired
};

export default Branch;
