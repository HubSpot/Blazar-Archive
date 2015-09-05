import React, {Component} from 'react';
import {bindAll} from 'underscore';

import BuildsSidebar from './BuildsSidebar.jsx';
import BuildsStore from '../../stores/buildsStore';
import BuildsActions from '../../actions/buildsActions';

import StarActions from '../../actions/starActions';
import StarStore from '../../stores/starStore';

import Sidebar from './Sidebar.jsx';
// import BuildsNotifier from '../BuildsNotifier';

class BuildsSidebarContainer extends Component {

  constructor(props) {
    super(props);
    
    bindAll(this, 'persistStarChange');

    this.state = {
      builds: {
        grouped: [],
        modules: [],
        all: []
      },
      stars: [],
      loadingBuilds: true,
      loadingStars: true,
      loading: true
    };
  }

  componentDidMount() {
    this.unsubscribeFromBuilds = BuildsStore.listen(this.onStatusChange.bind(this));
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange.bind(this));

    BuildsActions.loadBuilds();
    StarActions.loadStars();
  }

  componentWillUnmount() {
    this.unsubscribeFromBuilds();
    this.unsubscribeFromStars();
  }

  onStatusChange(state) {
    this.setState(state);

    if (!this.state.loadingBuilds && !this.state.loadingStars) {
      this.setState({
        loading: false
      })
    }

  }

  persistStarChange(isStarred, moduleId) {
    StarActions.toggleStar(isStarred, moduleId);
  }

  render() {
    // BuildsNotifier.updateModules(this.state.builds.modules);
    return (
      <Sidebar>
        <BuildsSidebar
          builds={this.state.builds}
          stars={this.state.stars}
          loading={this.state.loading} 
          persistStarChange={this.persistStarChange} />
      </Sidebar>
    );
  }
}

export default BuildsSidebarContainer;
