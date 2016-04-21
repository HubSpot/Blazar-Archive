import React, { Component, PropTypes } from 'react';

class CardStack extends Component {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div className='card-stack'>
        <div className='card-stack__wrapper'>
          {this.props.children}
        </div>
      </div>
    );
  }
}

CardStack.propTypes = {
  children: PropTypes.node
};

export default CardStack;