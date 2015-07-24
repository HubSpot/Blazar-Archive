import React, {Component, PropTypes} from 'react';
import Org from './Org.jsx';
import PageContainer from '../layout/PageContainer.jsx';

class OrgContainer extends Component {

  constructor(props) {
    super(props);

    this.state = {
      loading: true
    };
  }

  render() {
    return (
      <PageContainer>
        <Org
          params={this.props.params}
          loading={this.state.loading}
        />
      </PageContainer>
    );
  }
}

OrgContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default OrgContainer;
