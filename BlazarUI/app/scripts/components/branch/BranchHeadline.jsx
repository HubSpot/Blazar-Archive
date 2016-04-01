/* global config */
import React, {Component, PropTypes} from 'react';
import {contains} from 'underscore';
import Image from '../shared/Image.jsx'
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
    const imgPath = `${config.apiRoot}/branches/state/${this.props.params.branchId}/shield`;
    const {stars, params, currentRepoBuild} = this.props;
    const branchId = parseInt(this.props.params.branchId, 10);

    return (
      <Headline>
        <Star
          className='icon-roomy'
          isStarred={contains(stars, branchId)}
          id={branchId}
        />
        <Icon type="octicon" name="git-branch" classNames="headline-icon" />
        {this.props.branchInfo.repository} - {this.props.branchInfo.branch}
        <HeadlineDetail>
          Branch Builds 
          <Image src={imgPath}/>
        </HeadlineDetail>
      </Headline>
    )
  }
}

RepoBuildHeadline.propTypes = {
  params: PropTypes.object.isRequired,
  branchId: PropTypes.number,
  loading: PropTypes.bool.isRequired,
  branchInfo: PropTypes.object.isRequired
};

export default RepoBuildHeadline;
