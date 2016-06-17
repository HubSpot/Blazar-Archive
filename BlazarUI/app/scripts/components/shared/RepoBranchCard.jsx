import React, {Component} from 'react';
import {Card} from 'card-stack-test';

import RepoBranchCardSummary from '../shared/RepoBranchCardSummary.jsx';
import RepoBranchCardDetails from '../shared/RepoBranchCardDetails.jsx';

function RepoBranchCard(props) {
  const RenderedCard = Card(RepoBranchCardSummary, RepoBranchCardDetails);

  return <RenderedCard {...props} />;
}

export default RepoBranchCard;
