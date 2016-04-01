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
    
    const {stars, params, currentRepoBuild} = this.props;
    console.log(currentRepoBuild);

    return (
      <Headline>
        <Star
          className='icon-roomy'
          isStarred={contains(stars, this.props.branchId)}
          id={this.props.branchId}
        />
        {params.repo}
        <HeadlineDetail>
          - {params.branch} build #{currentRepoBuild.buildNumber}
        </HeadlineDetail>
      </Headline>
    )
  }
}

RepoBuildHeadline.propTypes = {
  params: PropTypes.object.isRequired,
  branchId: PropTypes.number,
  loading: PropTypes.bool.isRequired
};

export default RepoBuildHeadline;
