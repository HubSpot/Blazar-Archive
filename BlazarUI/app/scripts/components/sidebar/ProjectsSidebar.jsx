import React from 'react';
import ProjectsSidebarListItem from './ProjectsSidebarListItem.jsx';
import SidebarFilter from './SidebarFilter.jsx';
import ComponentHelpers from '../ComponentHelpers';

class ProjectsSidebar extends React.Component {

  constructor(props) {
    super(props);
    ComponentHelpers.bindAll(this, ['updateResults']);
  }

  updateResults(input) {
    // To do: expand repos to show modules searched for
    console.log('input change: ', input);
  }

  render() {
    // To do: replcae loading text with animation
    let loading = this.props.loading ? <div>Loading Projects...</div> : '';
    let groupedRepos = this.props.builds.grouped;

    let sidebarRepoList = [];
    groupedRepos.forEach( (repo) => {
      sidebarRepoList.push(
        <ProjectsSidebarListItem key={repo.name} repo={repo} />
      );
    })


    return (
      <div>
        {loading}
        <SidebarFilter
          loading={this.props.loading}
          repos={groupedRepos}
          modules={this.props.builds.modules}
          updateResults={this.updateResults}
        />
        <div className='sidebar__list'>
          {sidebarRepoList}
        </div>
      </div>
    );

  }

}

ProjectsSidebar.propTypes = {
  loading: React.PropTypes.bool,
  builds: React.PropTypes.object
};

export default ProjectsSidebar;
