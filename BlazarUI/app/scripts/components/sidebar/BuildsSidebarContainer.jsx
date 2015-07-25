import React, {Component} from 'react';
import BuildsSidebar from './BuildsSidebar.jsx';
import BuildsStore from '../../stores/buildsStore';
import BuildsActions from '../../actions/buildsActions';
import Sidebar from './Sidebar.jsx';

class BuildsSidebarContainer extends Component {

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
        <BuildsSidebar builds={this.state.builds} loading={this.state.loading} />
      </Sidebar>
    );
  }
}

export default BuildsSidebarContainer;
