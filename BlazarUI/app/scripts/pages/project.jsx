import React from 'react';
import UIGrid from '../components/shared/grid/UIGrid.jsx'
import UIGridItem from '../components/shared/grid/UIGridItem.jsx'

import ProjectSidebarContainer from '../components/sidebar/project/ProjectSidebarContainer.jsx'
import ProjectMain from '../components/project/Project.jsx'

class Project extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <UIGrid>

        <UIGridItem size={3}>
          <ProjectSidebarContainer
            projectId={this.props.params.id}
          />
        </UIGridItem>

        <UIGridItem size={8}>
          <ProjectMain
            projectId={this.props.params.id}
          />
        </UIGridItem>

      </UIGrid>
    );
  }
}

export default Project;



