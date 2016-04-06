/*global config*/
import React, {Component, PropTypes} from 'react';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Loader from '../shared/Loader.jsx';
import {Link} from 'react-router';
import {buildResultIcon, humanizeText, timestampDuration, buildIsInactive} from '../Helpers';

class BuildHeadline extends Component {

  renderTimestampDurationMaybe() {
    const {build} = this.props.data;

    if (buildIsInactive(build.state)) {
      return `(in ${timestampDuration(build.startTimestamp, build.endTimestamp)})`;
    }

    return '';
  }

  render() {
    if (this.props.loading) {
      return null;
    }
    
    const {moduleName} = this.props.params;
    const {build} = this.props.data;
    const buildLink = `${config.appRoot}/builds/branch/${this.props.params.branchId}/build/${this.props.params.buildNumber}`;
  
    return (
        <Headline className='build__headline'>
          {buildResultIcon(this.props.data.build.state)}
          <div className="build-headline">
            {moduleName}
            <HeadlineDetail>
              Status: {humanizeText(build.state)} { ' ' } {this.renderTimestampDurationMaybe()}<br />
              <Link to={buildLink}>&lt; back to build #{this.props.params.buildNumber}</Link>
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
