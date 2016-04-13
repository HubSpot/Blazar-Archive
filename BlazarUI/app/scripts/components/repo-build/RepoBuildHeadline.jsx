/*global config*/
import React, {Component, PropTypes} from 'react';
import {contains} from 'underscore';
import {Link} from 'react-router';
import {getIsStarredState} from '../Helpers.js';
import {getPathname} from '../Helpers';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';

class RepoBuildHeadline extends Component {
    
  render() {
    if (this.props.loading || this.props.currentRepoBuild === undefined) {
      return null;
    }
    
    const {stars, params, currentRepoBuild, branchInfo} = this.props;
    const branchId = parseInt(params.branchId, 10);
    const branchLink = `/builds/branch/${this.props.params.branchId}`;
    const repoLink = `/builds/repo/${this.props.branchInfo.repository}`;

    return (
      <Headline className='repobuild-headline headline--no-padding'>
        <HeadlineDetail crumb={true} block={true}>
          <Link to={repoLink}>{this.props.branchInfo.repository}</Link>
          &nbsp;&gt;&nbsp;
          <Link to={branchLink}>{this.props.branchInfo.branch}</Link><br />
        </HeadlineDetail>
        <span style={{'paddingLeft' : '5px'}}>Build #{currentRepoBuild.buildNumber}</span>
      </Headline>
    )
  }
}

RepoBuildHeadline.propTypes = {
  params: PropTypes.object.isRequired,
  loading: PropTypes.bool.isRequired,
  branchInfo: PropTypes.object.isRequired
};

export default RepoBuildHeadline;
