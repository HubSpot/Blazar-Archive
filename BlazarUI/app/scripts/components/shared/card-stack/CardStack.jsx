import React, { PropTypes } from 'react';
import classNames from 'classnames';

const CardStack = ({children, condensed, className}) => {
  const classes = classNames(
    'card-stack', {
      'card-stack--condensed': condensed
    },
    className
  );

  return (
    <div className={classes}>
      {children}
    </div>
  );
};

CardStack.propTypes = {
  children: PropTypes.node,
  className: PropTypes.string,
  condensed: PropTypes.bool
};

CardStack.defaultProps = {
  condensed: false
};

export default CardStack;
