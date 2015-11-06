import React, {Component, PropTypes} from 'react';
import CommitsTable from './CommitsTable.jsx';
import Loader from '../shared/Loader.jsx';

import {has} from 'underscore';
import {timestampFormatted} from '../Helpers';
import MutedMessage from '../shared/MutedMessage.jsx';

class BuildCommits extends Component {

  render() {
    
    if (!this.props.showCommits) {
      return null;
    }

    if (!has(this.props.build.build,'commitInfo')) {
      return (
        <div>No commit info</div>
      )
    }

    if (this.props.loading) {
      return <Loader />;
    }

    return (
      <CommitsTable 
        commits={this.props.build.build.commitInfo.newCommits}
      />
    );
  }

}

BuildCommits.propTypes = {
  build: PropTypes.object,
  showCommits: PropTypes.bool.isRequired,
  loading: PropTypes.bool
};

export default BuildCommits;
