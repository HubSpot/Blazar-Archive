import React, {Component, PropTypes} from 'react';
import {getIsStarredState} from '../Helpers.js';
import {getPathname} from '../Helpers';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Star from '../shared/Star.jsx';
import StarActions from '../../actions/starActions';

class ModuleHeadline extends Component {

  toggleStar(isStarred, starInfo) {
    StarActions.toggleStar(isStarred, starInfo);
  }
    
  render() {
    if (this.props.loading) {
      return <div />
    }

    return (
      <Headline>
        <Star
          className='icon-roomy'
          isStarred={getIsStarredState(this.props.stars, this.props.params.moduleId)}
          toggleStar={this.toggleStar}
          modulePath={getPathname()}
          moduleName={this.props.params.module}
          moduleId={this.props.params.moduleId}
          updateWithState={true}
        />
        {this.props.params.module} 
        <HeadlineDetail>
          Build History
        </HeadlineDetail>
      </Headline>
    )
  }
}

ModuleHeadline.propTypes = {
  params: PropTypes.object.isRequired,
  loading: PropTypes.bool.isRequired
};

export default ModuleHeadline;
