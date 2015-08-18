import React, {Component, PropTypes} from 'react';
import OrgContainer from '../components/org/OrgContainer.jsx';

class Org extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div>
        <OrgContainer
          params={this.props.params}
        />
      </div>
    );
  }
}

Org.propTypes = {
  params: PropTypes.object.isRequired
};

export default Org;
