import React, {Component, PropTypes} from 'react';
import Copyable from './Copyable.jsx';
import Icon from './Icon.jsx';
import {githubShaLink, truncate} from '../Helpers';

class Sha extends Component {

  render() {
    console.log("we in hurrrr");
    const commitLink = githubShaLink({gitInfo: this.props.gitInfo, build: this.props.build});

    return (
      <span className='sha'>
        <a href={commitLink} target="_blank">{truncate(this.props.build.get('sha'), this.props.truncate)}</a>
        <Copyable text={this.props.build.sha} tooltip='Copy SHA'>
          <Icon type='octicon' classNames='icon-roomy fa-link' name='clippy' />
        </Copyable>
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
