import React, {Component, PropTypes} from 'react';
import {contains} from 'underscore';
import {getIsStarredState} from '../Helpers.js';
import {getPathname} from '../Helpers';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Star from '../shared/Star.jsx';

class RepoBuildHeadline extends Component {
    
  render() {
    if (this.props.loading || this.props.currentRepoBuild === undefined) {
      return null;
    }
    
    const {stars, params, currentRepoBuild, branchInfo} = this.props;
    const branchId = parseInt(params.branchId, 10);

    return (
      <Headline>
        <Star
          className='icon-roomy'
          isStarred={contains(stars, branchId)}
          id={branchId}
        />
        {branchInfo.repository}
        <HeadlineDetail>
          - {branchInfo.branch} build #{currentRepoBuild.buildNumber}
        </HeadlineDetail>
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
