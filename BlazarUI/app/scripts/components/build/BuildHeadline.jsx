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
    const buildLink = `/builds/branch/${this.props.params.branchId}/build/${this.props.params.buildNumber}`;
    const branchLink = `/builds/branch/${this.props.params.branchId}`;
    const repoLink = `/builds/repo/${this.props.branchInfo.repository}`;
  
    return (
        <Headline className='build__headline headline--no-padding'>
          <HeadlineDetail crumb={true} block={true}>
            <Link to={repoLink}>{this.props.branchInfo.repository}</Link>
            &nbsp;&gt;&nbsp;
            <Link to={branchLink}>{this.props.branchInfo.branch}</Link>
            &nbsp;&gt;&nbsp;
            <Link to={buildLink}>#{this.props.params.buildNumber}</Link>
          </HeadlineDetail>
          {buildResultIcon(this.props.data.build.state)}
          <div className="build-headline">
            {moduleName}
            <HeadlineDetail>
              Status: {humanizeText(build.state)} { ' ' } {this.renderTimestampDurationMaybe()}<br />
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
