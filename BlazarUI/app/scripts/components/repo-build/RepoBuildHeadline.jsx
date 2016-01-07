import React, {Component, PropTypes} from 'react';
import {contains} from 'underscore';
import {getIsStarredState} from '../Helpers.js';
import {getPathname} from '../Helpers';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Star from '../shared/Star.jsx';

class RepoBuildHeadline extends Component {
    
  render() {
    if (this.props.loading) {
      return null;
    }
    
    const {stars, params} = this.props;

    return (
      <Headline>
      <Star
        className='icon-roomy'
        isStarred={contains(stars, parseInt(params.repositoryId))}
        repositoryId={this.props.params.repositoryId}
      />
        {this.props.params.repo} - build id {params.repoBuildId}
        <HeadlineDetail></HeadlineDetail>
      </Headline>
    )
  }
}

RepoBuildHeadline.propTypes = {
  params: PropTypes.object.isRequired,
  loading: PropTypes.bool.isRequired
};

export default RepoBuildHeadline;
