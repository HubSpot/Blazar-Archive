import React, {Component, PropTypes} from 'react';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Loader from '../shared/Loader.jsx';

class BuildHeadline extends Component {
  
  render() {
    if (this.props.loading) {
      return null;
    }
    
      return (
          <Headline>
            Module Name here
            <HeadlineDetail>
              Build number here
            </HeadlineDetail>
          </Headline>
      )

    // return (
    //   <Headline>
    //     <Star
    //       className='icon-roomy'
    //       isStarred={this.props.isStarred}
    //       toggleStar={this.props.toggleStar} 
    //       modulePath={this.props.modulePath}
    //       moduleName={this.props.moduleName}
    //       moduleId={this.props.moduleId}
    //       updateWithState={true}
    //       loading={this.props.loadingStars}
    //     />
    //     {this.props.moduleName}
    //     <HeadlineDetail>
    //       Build #{this.props.buildNumber}
    //     </HeadlineDetail>
    //   </Headline>
    // );
  }
}

// BuildHeadline.propTypes = {
//   loading: PropTypes.bool.isRequired,
//   moduleName: PropTypes.string.isRequired,
//   moduleId: PropTypes.number.isRequired,
//   modulePath: PropTypes.string.isRequired,
//   buildNumber: PropTypes.number.isRequired
// };

export default BuildHeadline;
