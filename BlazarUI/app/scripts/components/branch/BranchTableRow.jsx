import React from 'react';
import { Link } from 'react-router';
import Icon from '../shared/Icon.jsx';
import {labels, iconStatus} from '../constants';

class BranchTableRow extends React.Component {

  getRowClassNames() {
    if (this.props.module.buildState.result === 'FAILED') {
      return 'bgc-danger';
    }
  }

  getBuildResult() {
    let result = this.props.module.buildState.result;
    let classNames = `icon-roomy ${labels[result]}`;

    return (
      <Icon
        name={iconStatus[result]}
        classNames={classNames}
      />
    );
  }

  render() {
    let {
      buildState,
      module,
      modulePath
    } = this.props.module;

    return (
      <tr className={this.getRowClassNames()}>
      <td className='build-result-link'>
        {this.getBuildResult()}
        <Link to={buildState.buildLink}>{buildState.buildNumber}</Link>
      </td>
        <td>
          <Link to={modulePath}>{module.name}</Link>
        </td>
      </tr>
    );
  }
}

BranchTableRow.propTypes = {
  module: React.PropTypes.object.isRequired
};

export default BranchTableRow;
