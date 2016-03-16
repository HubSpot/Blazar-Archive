import React, {Component, PropTypes} from 'react';
import Icon from './Icon.jsx';
import {githubShaLink, truncate} from '../Helpers';

class Sha extends Component {

  render() {
    const commitLink = githubShaLink({gitInfo: this.props.gitInfo, build: this.props.build});

    return (
      <span className='sha'>
        <a href={commitLink} className='sha-link' target="_blank">{truncate(this.props.build.sha, this.props.truncate)}{ '  ' }</a>
      </span>
    );
  }

}

Sha.defaultProps = {
  truncate: 10
};

Sha.propTypes = {
  gitInfo: PropTypes.object.isRequired,
  build: PropTypes.object.isRequired,
  truncate: PropTypes.number
};

export default Sha;
