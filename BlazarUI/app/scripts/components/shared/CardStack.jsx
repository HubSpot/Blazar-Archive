import React, { Component, PropTypes } from 'react';
import $ from 'jquery';

import Loader from './Loader.jsx';

class CardStack extends Component {
  constructor(props) {
    super(props);
  }

  render() {

    let innerContent;

    if (this.props.loading) {
      return (
        <div className='card-stack'>
          <Loader align='center' roomy={true} />
        </div>
      );
    }
    
    else if (this.props.children.size === 0) {
      return (
        <div className='card-stack'>
          <div className='card-stack__wrapper'>
            {this.props.zeroState}
          </div>
        </div>
      );
    }

    return (
      <div className='card-stack'>
        <div className='card-stack__wrapper'>
          {this.props.header}
          {this.props.children}
        </div>
      </div>
    );
  }
}

CardStack.propTypes = {
  loading: PropTypes.bool.isRequired,
  header: PropTypes.node,
  zeroState: PropTypes.node,
  children: PropTypes.node
};

export default CardStack;