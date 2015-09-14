import React, {Component, PropTypes} from 'react';
import EmptyMessage from '../shared/EmptyMessage.jsx';
import { Link } from 'react-router';

class NoBuildHistory extends Component {

  render() {
    const repo = this.props.modulePath.split('/')[4];
    const branch = this.props.modulePath.split('/')[5];

    return (
      <EmptyMessage simple={true}>
        <p>
          <strong>No build history for:</strong>
        </p>
        <span className='crumb'>
          {repo}
        </span>
        <span className='crumb'>
          {branch}
        </span>
        <Link to={this.props.modulePath}>
          {this.props.moduleName}
        </Link>
      </EmptyMessage>
    )
  }
}

NoBuildHistory.propTypes = {
  moduleName: PropTypes.string.isRequired,
  modulePath: PropTypes.string.isRequired
}

export default NoBuildHistory;
