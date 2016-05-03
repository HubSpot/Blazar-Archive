import React, { Component, PropTypes } from 'react';

class RepoBranchCardStackHeader extends Component {

  render() {
    return (
      <div className='repo-branch-card__header'>
        <div>
          <div className='repo-branch-card__header-info'>
            <span />
          </div>
          <div className='repo-branch-card__header-last-built'>
            <span />
          </div>
          <div className='repo-branch-card__header-details'>
            <span />
          </div>
        </div>
      </div>
    );
  }
}

export default RepoBranchCardStackHeader;