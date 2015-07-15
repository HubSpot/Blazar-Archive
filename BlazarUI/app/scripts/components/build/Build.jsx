import React from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import BuildDetail from '../shared/BuildDetail.jsx';
import Log from './log.jsx';

class Build extends React.Component{

  render() {
    return (
      <div>
        <PageHeader>
          <h2 className='header-primary'>{this.props.params.module} <span className='header-subheader'> Build #{this.props.params.buildNumber} </span> </h2>
          <Breadcrumb />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <BuildDetail />
          </UIGridItem>
          <UIGridItem size={12}>
            <Log />
          </UIGridItem>
        </UIGrid>
      </div>
    );
  }

}

Build.defaultProps = { loading: true };

Build.propTypes = {
  params: React.PropTypes.object.isRequired
};

export default Build;
