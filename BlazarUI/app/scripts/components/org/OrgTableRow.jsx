import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';
import classNames from 'classnames';

import BuildStates from '../../constants/BuildStates.js';
import {tableRowBuildState, humanizeText, timestampFormatted, buildResultIcon} from '../Helpers';
import Sha from '../shared/Sha.jsx';

class OrgTableRow extends Component {

  constructor(props, context) {
    super(props, context);
  }

  getRowClassNames(state) {
    return classNames([
      tableRowBuildState(state),
      'clickable-table-row'
    ]);
  }

  onTableClick(blazarPath, e) {
    const link = e.target.className;

    if (link === 'repo-link' || link === 'build-link') {
      return;
    }

    else if (link === 'sha-link') {
      window.open(e.target.href, '_blank');
    }

    else if (blazarPath !== undefined) {
      this.context.router.push(blazarPath);
    }
  }

  render() {

    const build = this.props.data.get('lastBuild').toJS();
    const gitInfo = this.props.data.get('gitInfo').toJS();

    let buildLink, sha;

    if (build.blazarPath) {
      buildLink = (
        <Link className='build-link' to={build.blazarPath}>
          {build.buildNumber}
        </Link>
      );
    }

    if (build.sha !== undefined) {
      sha = <Sha gitInfo={gitInfo} build={build} />;
    }

    return (
      <tr onClick={this.onTableClick.bind(this, build.blazarPath)} className={this.getRowClassNames(build.state)}>
        <td className='build-status'>
          {buildResultIcon(build.state)}
        </td>
        <td>
          <Link className='repo-link' to={this.props.data.get('blazarRepositoryPath')}>
            {this.props.data.get('repository')}
          </Link>
        </td>
        <td>
          {buildLink}
        </td>
        <td>
          {timestampFormatted(build.startTimestamp)}
        </td>
        <td>
          {build.duration}
        </td>
        <td>
          {sha}
        </td>
      </tr>
    );
  }

}

OrgTableRow.contextTypes = {
  router: PropTypes.object.isRequired
};

OrgTableRow.propTypes = {
  data: PropTypes.object.isRequired
};

export default OrgTableRow;
