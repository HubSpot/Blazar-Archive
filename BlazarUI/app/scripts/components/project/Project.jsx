import React from 'react';
import PageHeader from '../shared/PageHeader.jsx'
import PageSection from '../shared/PageSection.jsx'
import Breadcrumb from '../shared/Breadcrumb.jsx'
import UIGrid from '../shared/grid/UIGrid.jsx'
import UIGridItem from '../shared/grid/UIGridItem.jsx'

class Project extends React.Component{

  constructor(props, context) {
   super(props);
  }

  getBreadcrumbs(){
    let project = this.props.project;
    return [
      { title: `${project.url}`, link: `#project/${project.url}` },
      { title: `${project.org}`, link: `#project/${project.url}/${project.org}` },
      { title: `${project.repo}`, link: `#project/${project.url}/${project.org}/${project.repo}` },
      { title: `${project.branch}`, link: `#project/${project.url}/${project.org}/${project.repo}/${project.branch}` },
      { title: `${project.module}`, link: `#project/${project.url}/${project.org}/${project.repo}/${project.branch}/${project.module}` },
      { title: `${project.buildNumber}`},
    ]
  }

  render() {
    return (
      <div>
        <PageHeader>
          <h2 className='header-primary'>{this.props.project.module} - Build #{this.props.project.buildNumber}</h2>
          <Breadcrumb links={this.getBreadcrumbs()} />
        </PageHeader>
        <UIGrid>
          <UIGridItem size={12}>
            <PageSection headline='Job Detail Here'></PageSection>
          </UIGridItem>
          <UIGridItem size={12}>
            <PageSection headline='Log Tail here'></PageSection>
          </UIGridItem>
        </UIGrid>
      </div>
    );
  }

}


export default Project;

Project.defaultProps = { loading: true }

Project.propTypes = {
  project : React.PropTypes.object.isRequired
}
