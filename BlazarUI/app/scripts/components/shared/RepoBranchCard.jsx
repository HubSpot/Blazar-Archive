import React, {Component} from 'react';
import Card from '../shared/card-stack/Card.jsx';

import RepoBranchCardSummary from '../shared/RepoBranchCardSummary.jsx';
import RepoBranchCardDetails from '../shared/RepoBranchCardDetails.jsx';

function RepoBranchCard({item, moduleBuildsList, expanded, onClick}) {
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
      details={expanded ? details : null}
      expanded={expanded}
      onClick={onClick}
    />
  );
}

export default RepoBranchCard;
