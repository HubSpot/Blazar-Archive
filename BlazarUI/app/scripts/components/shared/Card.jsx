import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';

class Card extends Component {

  getBaseClassNames() {
    return classNames([
      'card-stack__card', {
        'card-stack__card--expanded': this.props.expanded,
        'card-stack__card--first': this.props.first,
        'card-stack__card--last': this.props.last,
        'card-stack__card--below-expanded': this.props.belowExpanded
      }]);
  }

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