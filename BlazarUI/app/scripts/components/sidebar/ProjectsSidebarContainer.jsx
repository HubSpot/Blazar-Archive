import React, {Component} from 'react';
import ProjectsSidebar from './ProjectsSidebar.jsx';
import BuildsStore from '../../stores/buildsStore';
import BuildsActions from '../../actions/buildsActions';
import Sidebar from './Sidebar.jsx';

class ProjectsSidebarContainer extends Component {

  constructor(props) {
    super(props);

    this.state = {
      builds: {
        grouped: [],
        modules: []
      },
      loading: false
    };
  }

  componentDidMount() {
    this.unsubscribe = BuildsStore.listen(this.onStatusChange.bind(this));
    BuildsActions.loadBuilds();
  }

  componentWillUnmount() {
    this.unsubscribe();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  render() {
    return (
      <Sidebar>
        <ProjectsSidebar builds={this.state.builds} loading={this.state.loading} />
      </Sidebar>
    );
  }
}

export default ProjectsSidebarContainer;
