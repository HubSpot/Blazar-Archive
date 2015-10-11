import React, {Component, PropTypes} from 'react';
import CommitsTable from './CommitsTable.jsx';
import Loader from '../shared/Loader.jsx';
import Collapsable from '../shared/Collapsable.jsx';
import {has} from 'underscore';
import {timestampFormatted} from '../Helpers';
import MutedMessage from '../shared/MutedMessage.jsx';

class BuildCommits extends Component {

  render() {

    if (!has(this.props.build.build,'commitInfo')) {
      return <div />;
    }

    const commitInfo = this.props.build.build.commitInfo;

    if (this.props.loading) {
      return <Loader />;
    }

    const inflection = commitInfo.newCommits.length !== 1 ? 's' : '';


    let header;

    if (commitInfo.newCommits.length === 0) {
      header = (
        <span>No new commits since previous build</span>
      );
    }

    else {
      header = (
        <span>
          <span className='badge roomy-x'>
            {commitInfo.newCommits.length}
          </span> 
          new commit{inflection} since previous build 
        </span>
      );
    }

    return (
      <Collapsable
        header={header}
        initialToggleStateOpen={false}
        disableToggle={commitInfo.newCommits.length === 0}
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
