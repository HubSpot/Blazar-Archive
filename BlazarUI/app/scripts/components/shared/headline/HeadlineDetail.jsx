import React, {PropTypes} from 'react';

const HeadlineDetail = ({children, block, crumb}) => {
  let extraClass = `headline__detail${(block ? ' headline-block' : '')}`;
  extraClass += crumb ? ' headline-crumb' : '';

  return (
    <span className={extraClass}>
      {' '} <span className="headline__detail-subheadline">{children}</span>
    </span>
  );
};

HeadlineDetail.propTypes = {
  children: PropTypes.node,
  block: PropTypes.bool,
  crumb: PropTypes.bool
};

export default HeadlineDetail;
