/* global config */
import React, {Component, PropTypes} from 'react';
import {contains} from 'underscore';
import {Link} from 'react-router';
import Image from '../shared/Image.jsx'
import {getIsStarredState} from '../Helpers.js';
import {getPathname} from '../Helpers';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Star from '../shared/Star.jsx';
import Icon from '../shared/Icon.jsx';

class RepoBuildHeadline extends Component {

  renderShield() {
    if (this.props.loading) {
      return null;
    }

    const imgPath = `${config.apiRoot}/branches/state/${this.props.params.branchId}/shield`;

    return (
      <div className='branch__shield'>
        <Image src={imgPath} />
      </div>
    );
  }
    
  render() {
    if (this.props.loading) {
      return null;
    }

    const {stars, params, currentRepoBuild} = this.props;
    const branchId = parseInt(this.props.params.branchId, 10);
    const repoLink = `/builds/repo/${this.props.branchInfo.repository}`;

    return (
      <Headline className='headline--no-padding'>
        <HeadlineDetail block={true}>
          <Link to={repoLink}>&lt; Back to {this.props.branchInfo.repository}</Link>
        </HeadlineDetail>
        <Star
          className='icon-roomy'
          isStarred={contains(stars, branchId)}
          id={branchId}
        />
        <Icon type="octicon" name="git-branch" classNames="headline-icon" />
        {this.props.branchInfo.repository} - {this.props.branchInfo.branch}
        {this.renderShield()}
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
