import React from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import ModuleList from './ModuleList.jsx';

const BranchState = ({activeModules, inactiveModules}) => {
  return (
      <PageContainer documentTitle="test-page">
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
  activeModules: ImmutablePropTypes.list,
  inactiveModules: ImmutablePropTypes.list
};

export default BranchState;
