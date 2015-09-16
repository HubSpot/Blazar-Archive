import React, {Component,PropTypes} from 'react';
import {bindAll} from 'underscore';

import BuildsSidebar from './BuildsSidebar.jsx';
import BuildsStore from '../../stores/buildsStore';
import BuildsActions from '../../actions/buildsActions';

import StarActions from '../../actions/starActions';
import StarStore from '../../stores/starStore';

import Sidebar from './Sidebar.jsx';
import SidebarLogo from './SidebarLogo.jsx';
// import BuildsNotifier from '../BuildsNotifier';

class BuildsSidebarContainer extends Component {

  constructor(props) {
    super(props);

    bindAll(this, 'persistStarChange');

    this.state = {
      builds: [],
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

  persistStarChange(isStarred, starInfo) {
    StarActions.toggleStar(isStarred, starInfo);
  }

  render() {
    let miniLogo;
    if (this.props.isCollapsed) {
      miniLogo = <SidebarLogo mini={true} />
    }
    // BuildsNotifier.updateModules(this.state.builds.modules);
    return (
      <div>
        {miniLogo}
        <Sidebar collapse={this.props.collapse} isCollapsed={this.props.isCollapsed}>
          <BuildsSidebar
            builds={this.state.builds}
            stars={this.state.stars}
            loading={this.state.loading}
            persistStarChange={this.persistStarChange} />
        </Sidebar>
      </div>
    );
  }
}

BuildsSidebarContainer.propTypes = {
  collapse: PropTypes.func,
  isCollapsed: PropTypes.bool
};


export default BuildsSidebarContainer;
