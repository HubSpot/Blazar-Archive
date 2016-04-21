import React, { Component, PropTypes } from 'react';

class Card extends Component {

  // Implemented by child classes
  render() {
    return null;
  }
}

Card.propTypes = {
  first: PropTypes.bool.isRequired,
  last: PropTypes.bool.isRequired,
  onClick: PropTypes.func.isRequired,
  expanded: PropTypes.bool.isRequired,
  belowExpanded: PropTypes.bool.isRequired
};

export default Card;