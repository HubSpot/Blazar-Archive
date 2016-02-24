import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';

import BuildStates from '../../constants/BuildStates.js';
import {tableRowBuildState, humanizeText, timestampFormatted, buildResultIcon} from '../Helpers';
import Sha from '../shared/Sha.jsx';

class OrgTableRow extends Component {

  render() {

    const build = this.props.data.get('lastBuild').toJS();
    const gitInfo = this.props.data.get('gitInfo').toJS();

    let buildLink, sha;

    if (build.blazarPath) {
      buildLink = (
        <Link to={build.blazarPath}>
          {build.buildNumber}
        </Link>
      );
    }

    if (build.sha !== undefined) {
      sha = <Sha gitInfo={gitInfo} build={build} />;
    }

    return (
      <tr className={tableRowBuildState(build.state)}>
        <td className='build-status'>
          {buildResultIcon(build.state)}
        </td>
        <td>
          <Link to={this.props.data.get('blazarRepositoryPath')}>
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

OrgTableRow.propTypes = {
  data: PropTypes.object.isRequired
};

export default OrgTableRow;
