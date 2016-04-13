/*global config*/
import React, {Component, PropTypes} from 'react';
import {contains} from 'underscore';
import {Link} from 'react-router';
import {getIsStarredState} from '../Helpers.js';
import {getPathname} from '../Helpers';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import SimpleBreadcrumbs from '../shared/SimpleBreadcrumbs.jsx';

class RepoBuildHeadline extends Component {
    
  render() {
    if (this.props.loading || this.props.currentRepoBuild === undefined) {
      return null;
    }
    
    const {stars, params, currentRepoBuild, branchInfo} = this.props;
    const branchId = parseInt(params.branchId, 10);

    return (
      <div>
        <SimpleBreadcrumbs 
          repo={true} 
          branch={true}
          {...this.props} />
        <Headline className='repobuild-headline'>
          <span>Build #{currentRepoBuild.buildNumber}</span>
        </Headline>
      </div>
    )
  }
}

RepoBuildHeadline.propTypes = {
  params: PropTypes.object.isRequired,
  loading: PropTypes.bool.isRequired,
  branchInfo: PropTypes.object.isRequired
};

export default RepoBuildHeadline;
