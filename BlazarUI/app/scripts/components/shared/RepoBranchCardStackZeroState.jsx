import React, { Component, PropTypes } from 'react';

class RepoBranchCardStackZeroState extends Component {
  render() {
    return (
      <div className='card-stack__zero-state'>
        <h1>You haven't starred any branches yet.</h1>
        <p>You should star some branches if you want them to appear in this awesome new dashboard.</p>
      </div>
    );
  }
}

export default RepoBranchCardStackZeroState;