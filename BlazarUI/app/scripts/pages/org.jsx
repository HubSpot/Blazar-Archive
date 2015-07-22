import React from 'react';
import OrgContainer from '../components/org/OrgContainer.jsx';

class Org extends React.Component {

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
  params: React.PropTypes.object.isRequired
};

export default Org;
