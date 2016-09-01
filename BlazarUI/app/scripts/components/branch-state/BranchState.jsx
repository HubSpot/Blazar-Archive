import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { routerShape } from 'react-router';

import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import ModuleList from './ModuleList.jsx';
import BranchStateHeadline from './BranchStateHeadline.jsx';

const BranchState = ({activeModules, inactiveModules, branchId, router}) => {
  const onBranchSelect = (selectedBranchId) => {
    router.push(`/branchState/branch/${selectedBranchId}`);
  };

  return (
      <PageContainer documentTitle="test-page">
        <BranchStateHeadline branchId={branchId} onBranchSelect={onBranchSelect} />
        <UIGrid>
          <UIGridItem size={12}>
            <h2>Active modules</h2>
            <ModuleList modules={activeModules} />
            <h2>Inactive modules</h2>
            <ModuleList modules={inactiveModules} />
          </UIGridItem>
        </UIGrid>
      </PageContainer>
  );
};

BranchState.propTypes = {
  branchId: PropTypes.number.isRequired,
  activeModules: ImmutablePropTypes.list,
  inactiveModules: ImmutablePropTypes.list,
  router: routerShape
};

export default BranchState;
