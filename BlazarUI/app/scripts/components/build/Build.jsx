import React from 'react';
import config from '../../config.js';
import PageHeader from '../shared/PageHeader.jsx';
import PageSection from '../shared/PageSection.jsx';
import Breadcrumb from '../shared/Breadcrumb.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';

class Build extends React.Component{

  constructor(props, context) {
   super(props);
  }

  getBreadcrumbs(){
    let params = this.props.params;
    return [
      { title: `${params.url}`, link: `${config.appRoot}/${params.url}` },
      { title: `${params.org}`, link: `${config.appRoot}/${params.url}/${params.org}` },
      { title: `${params.repo}`, link: `${config.appRoot}/${params.url}/${params.org}/${params.repo}` },
      { title: `${params.branch}`, link: `${config.appRoot}/${params.url}/${params.org}/${params.repo}/${params.branch}` },
      { title: `${params.module}`, link: `${config.appRoot}/${params.url}/${params.org}/${params.repo}/${params.branch}/${params.module}` },
      { title: `${params.buildNumber}`}
    ]
  }

  render() {
    return (
      <div>
        <PageHeader>
          <h2 className='header-primary'>{this.props.params.module} <span className='header-subheader'> / Build #{this.props.params.buildNumber} </span> </h2>
          <Breadcrumb links={this.getBreadcrumbs()} />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <PageSection headline='Build Detail Here'></PageSection>
          </UIGridItem>
          <UIGridItem size={12}>
            <PageSection headline='Log Tail here'></PageSection>
          </UIGridItem>
        </UIGrid>
      </div>
    );
  }

}


export default Build;

Build.defaultProps = { loading: true }

Build.propTypes = {
  params : React.PropTypes.object.isRequired
}
