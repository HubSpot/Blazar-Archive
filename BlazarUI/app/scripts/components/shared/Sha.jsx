import React, {Component, PropTypes} from 'react';
import Copyable from './Copyable.jsx';
import Icon from './Icon.jsx';
import Helpers from '../ComponentHelpers';

class Sha extends Component {

  render() {

    const commitLink = Helpers.githubShaLink({gitInfo: this.props.gitInfo, build: this.props.build});

    return (
      <span>
        <Copyable text={this.props.build.sha}>
          <Icon type='octicon' classNames='icon-roomy fa-link' name='clippy' />
        </Copyable>
        <a href={commitLink} target="_blank">{Helpers.truncate(this.props.build.sha, this.props.truncate)}</a>
      </span>
    );
  }

}

Sha.defaultProps = {
  truncate: 8
};

Sha.propTypes = {
  gitInfo: PropTypes.object.isRequired,
  build: PropTypes.object.isRequired,
  truncate: PropTypes.number
};

export default Sha;
