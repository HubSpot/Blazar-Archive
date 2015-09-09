import React, {Component, PropTypes} from 'react';
import CommitsTable from './CommitsTable.jsx';
import SectionLoader from '../shared/SectionLoader.jsx';
import Collapsable from '../shared/Collapsable.jsx';
import {has} from 'underscore';
import {timestampFormatted} from '../Helpers';

class BuildCommits extends Component {

  render() {
    if (this.props.loading) {
      return <SectionLoader />;
    }


    let commitHistory;
    
    if (this.props.loading) {
      commitHistory = (
        <p>Loading commits pushed since this build</p>
      )
    }

    return (
      <Collapsable
        header='Commits since this build'
        initialToggleStateOpen={true}
        pad={true}
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
