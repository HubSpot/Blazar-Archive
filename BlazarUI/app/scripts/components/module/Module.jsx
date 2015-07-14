import React from 'react';
import config from '../../config.js';
import PageHeader from '../shared/PageHeader.jsx';
import PageSection from '../shared/PageSection.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import LastBuild from '../shared/BuildDetail.jsx';
import BuildHistoryTable from './BuildHistoryTable.jsx';

class Module extends React.Component{

  constructor(props, context) {
   super(props);
  }

  render() {
    return (
      <div>
        <PageHeader>
          <h2 className='header-primary'> {this.props.params.module} <span className='header-subheader'> Build History  </span> </h2>
          <Breadcrumb />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <LastBuild />
            <BuildHistoryTable />
          </UIGridItem>
        </UIGrid>
      </div>
    );
  }

}


export default Module;

Module.defaultProps = { loading: true }

Module.propTypes = {
  params : React.PropTypes.object.isRequired
}
