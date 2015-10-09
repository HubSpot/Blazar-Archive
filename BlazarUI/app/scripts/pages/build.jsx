import React, {Component, PropTypes} from 'react';
import BuildContainer from '../components/build/BuildContainer.jsx';
import PageHeaderContainer from '../components/PageHeader/PageHeaderContainer.jsx';

class Project extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div>
        <PageHeaderContainer 
          params={this.props.params}
        />
        <BuildContainer
          params={this.props.params}
        />
      </div>
    );
  }
}

Project.propTypes = {
  params: PropTypes.object.isRequired
};

export default Project;
