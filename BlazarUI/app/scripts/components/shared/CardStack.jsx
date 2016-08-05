import React, { PropTypes } from 'react';

import Loader from './Loader.jsx';

const CardStack = ({loading, children, zeroState, header}) => {
  if (loading) {
    return (
      <div className="card-stack">
        <Loader align="center" roomy={true} />
      </div>
    );
  } else if (!children.size) {
    return zeroState;
  }

  return (
    <div className="card-stack">
      <div className="card-stack__wrapper">
        {header}
        {children}
      </div>
    </div>
  );
};

CardStack.propTypes = {
  loading: PropTypes.bool.isRequired,
  header: PropTypes.node,
  zeroState: PropTypes.node,
  children: PropTypes.node
};

export default CardStack;
