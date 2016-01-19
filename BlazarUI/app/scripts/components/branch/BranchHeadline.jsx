import React, {Component, PropTypes} from 'react';
import {contains} from 'underscore';
import {getIsStarredState} from '../Helpers.js';
import {getPathname} from '../Helpers';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Star from '../shared/Star.jsx';
import Icon from '../shared/Icon.jsx';

class RepoBuildHeadline extends Component {
    
  render() {
    if (this.props.loading) {
      return null;
    }
    
    const {stars, params, currentRepoBuild} = this.props;

    return (
      <Headline>
        <Star
          className='icon-roomy'
          isStarred={contains(stars, this.props.branchId)}
          id={this.props.branchId}
        />
        <Icon type="octicon" name="git-branch" classNames="headline-icon" />
        {this.props.params.repo} - {this.props.params.branch}
        <HeadlineDetail>
          Branch Builds
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
