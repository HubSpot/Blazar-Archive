import React, {Component, PropTypes} from 'react';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Star from '../shared/Star.jsx';

class BuildHeadline extends Component {
  
  render() {
    return (
      <Headline>
        <Star
          className='icon-roomy'
          isStarred={this.props.isStarred}
          toggleStar={this.props.toggleStar} 
          modulePath={this.props.modulePath}
          moduleName={this.props.moduleName}
          moduleId={this.props.moduleId}
          updateWithState={true}
        />
        {this.props.moduleName}
        <HeadlineDetail>
          Build #{this.props.buildNumber}
        </HeadlineDetail>
      </Headline>
    );
  }
}

BuildHeadline.propTypes = {
  moduleName: PropTypes.string.isRequired,
  moduleId: PropTypes.number.isRequired,
  modulePath: PropTypes.string.isRequired,
  buildNumber: PropTypes.number.isRequired,
  isStarred: PropTypes.bool.isRequired,
  toggleStar: PropTypes.func.isRequired,
};

export default BuildHeadline;
