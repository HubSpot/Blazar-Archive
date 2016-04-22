import React, { Component, PropTypes } from 'react';

class Card extends Component {

  // Implemented by child classes
  render() {
    return null;
  }
}

Card.propTypes = {
  onClick: PropTypes.func.isRequired,
  expanded: PropTypes.bool.isRequired,
  belowExpanded: PropTypes.bool.isRequired,
  first: PropTypes.bool,
  last: PropTypes.bool
};

export default Card;