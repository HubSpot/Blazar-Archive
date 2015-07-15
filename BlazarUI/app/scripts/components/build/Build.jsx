import React from 'react';
import PageHeader from '../shared/PageHeader.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import BuildDetail from '../shared/BuildDetail.jsx';
import Log from './log.jsx';

class Build extends React.Component{

  render() {
    let {module, buildNumber} = this.props.build;
    return (
      <div>
        <PageHeader>
          <h2 className='header-primary'>
            {module}{' '}
            <span className='header-subheader'>
              Build #{buildNumber}
            </span>
          </h2>
          <Breadcrumb />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <BuildDetail
              build={this.props.build}
            />
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
  build: React.PropTypes.object.isRequired
};

export default Build;
