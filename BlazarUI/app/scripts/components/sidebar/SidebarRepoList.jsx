import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import LazyRender from '../shared/LazyRender.jsx';
import Loader from '../shared/Loader.jsx';
import SidebarItem from './SidebarItem.jsx';
import {has} from 'underscore';

class SidebarRepoList extends Component {

  render() {    
    const {changingBuildsType, filteredBuilds, loading} = this.props;

    if (loading) {
      return null;
    }
    
    if (changingBuildsType) {
      return (
        <Loader align='top-center' className='sidebar-loader' />
      );
    }

    const buildsList = filteredBuilds.map( (build) => {
      const buildType = has(build, 'inProgressBuild') ? 'inProgressBuild' : has(build, 'lastBuild') ? 'lastBuild' : 'neverBuilt'

      return (
        <SidebarItem
          key={build[buildType].id}
          build={build[buildType]}
          prevBuildState={has(build, 'lastBuild') ? build['lastBuild'].state : false}
          gitInfo={build.gitInfo}
          isStarred={true}
        />
      );
    });

    return (
      <LazyRender
        childHeight={71} 
        maxHeight={this.props.sidebarHeight}>
        {buildsList}
      </LazyRender>
    );

  }
}

SidebarRepoList.propTypes = {
  sidebarHeight: PropTypes.number.isRequired,
  changingBuildsType: PropTypes.bool.isRequired,
  filteredBuilds: PropTypes.array.isRequired,
  loading: PropTypes.bool.isRequired
};

export default SidebarRepoList;
