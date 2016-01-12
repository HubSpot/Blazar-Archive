import React, {Component, PropTypes} from 'react';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Loader from '../shared/Loader.jsx';
import {renderBuildStatusIcon, humanizeText} from '../Helpers';

class BuildHeadline extends Component {

  render() {
    if (this.props.loading) {
      return null;
    }
    
    console.log(this.props);
    
    const {moduleName} = this.props.params;
    const {build} = this.props.data;
  
    return (
        <Headline>
          {renderBuildStatusIcon(this.props.data.build)}
          {moduleName}
          <HeadlineDetail>
            Status: {humanizeText(build.state)}
          </HeadlineDetail>
        </Headline>
    );
  }
}

BuildHeadline.propTypes = {
  loading: PropTypes.bool.isRequired,
  data: PropTypes.object.isRequired,
  params: PropTypes.object.isRequired
};

export default BuildHeadline;
