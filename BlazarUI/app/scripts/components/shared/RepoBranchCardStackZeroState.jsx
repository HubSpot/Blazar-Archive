import React from 'react';
import Image from '../shared/Image.jsx';

const RepoBranchCardStackZeroState = () => {
  const starImagePath = `${window.config.staticRoot}/images/sad-star.svg`;

  return (
    <div className="repo-branch-card-stack__zero-state">
      <Image src={starImagePath} />
      <h1>You haven't starred any branches</h1>
      <p>Branches that you’ve starred will normally show up here. You can use the navigation on
the left to find a branch, then star it for quick access later!</p>

    </div>
  );
};

export default RepoBranchCardStackZeroState;
