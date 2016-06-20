import React, {Component} from 'react';
import {Card} from 'card-stack-test';

import RepoBranchCardSummary from '../shared/RepoBranchCardSummary.jsx';
import RepoBranchCardDetails from '../shared/RepoBranchCardDetails.jsx';

function RepoBranchCard({item, moduleBuildsList, expanded, belowExpanded, onClick}) {
  const summary = (
    <RepoBranchCardSummary
      item={item}
    />
  );

  const details = (
    <RepoBranchCardDetails
      item={item}
      moduleBuildsList={moduleBuildsList}
    />
  );

  return (
    <Card
      summary={summary}
      details={details}
      expanded={expanded}
      belowExpanded={belowExpanded}
      onClick={onClick}
    />
  );
}

export default RepoBranchCard;
