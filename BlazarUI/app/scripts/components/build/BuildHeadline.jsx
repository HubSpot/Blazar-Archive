import React, {Component, PropTypes} from 'react';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Loader from '../shared/Loader.jsx';
import {buildResultIcon, humanizeText} from '../Helpers';

class BuildHeadline extends Component {

  render() {
    if (this.props.loading) {
      return null;
    }
    
    const {moduleName} = this.props.params;
    const {build} = this.props.data;
  
    return (
        <Headline>
          {buildResultIcon(this.props.data.build.state)}
          <div className="build-headline">
            {moduleName}
            <HeadlineDetail>
              Status: {humanizeText(build.state)}
            </HeadlineDetail>
          </div>
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
