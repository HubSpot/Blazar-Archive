/*global config*/
import React, { Component, PropTypes } from 'react';
import Image from '../shared/Image.jsx'

class RepoBranchCardStackZeroState extends Component {
  render() {

    const starImagePath = `${config.staticRoot}/images/sad-star.svg`;

    return (
      <div className='card-stack__zero-state'>
        <Image src={starImagePath} />
        <h1>You haven't starred any branches yet</h1>
        <p>Branches that you’ve starred will normally show up here. Use the navigation on 
the left to find a branch.</p>
      </div>
    );
  }
}

export default RepoBranchCardStackZeroState;