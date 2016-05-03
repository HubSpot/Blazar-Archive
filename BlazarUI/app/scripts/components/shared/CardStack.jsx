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
      innerContent = (
         <Loader align='center' roomy={true} />
      );
    }
    
    else if (this.props.children.size === 0) {
      innerContent = (
        <div className='card-stack__wrapper'>
          {this.props.zeroState}
        </div>
      );
    }

    else {
      innerContent = (
        <div className='card-stack__wrapper'>
          {this.props.header}
          {this.props.children}
        </div>
      );
    }

    return (
      <div className='card-stack'>
        {innerContent}
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