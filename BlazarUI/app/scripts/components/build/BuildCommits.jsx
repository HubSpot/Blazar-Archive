import React, {Component, PropTypes} from 'react';
import CommitsTable from './CommitsTable.jsx';
import SectionLoader from '../shared/SectionLoader.jsx';
import Collapsable from '../shared/Collapsable.jsx';
import {has} from 'underscore';
import {timestampFormatted} from '../Helpers';

class BuildCommits extends Component {

  render() {
    let commitHistory;

    if (!has(this.props.build.build,'commitInfo')) {
      return <div />;
    }

    const commitInfo = this.props.build.build.commitInfo;

    if (this.props.loading) {
      return <SectionLoader />;
    }
    
    if (this.props.loading) {
      commitHistory = (
        <p>Loading commits pushed since this build</p>
      )
    }

    const header = (
      <span> ({commitInfo.newCommits.length}) new commits since this build </span>
    )

    return (
      <Collapsable
        header={header}
        initialToggleStateOpen={false}
      >
        <CommitsTable 
          commits={this.props.build.build.commitInfo.newCommits}
        />

      </Collapsable>
    );
  }

}

BuildCommits.propTypes = {
  build: PropTypes.object,
  loading: PropTypes.bool
};

export default BuildCommits;
