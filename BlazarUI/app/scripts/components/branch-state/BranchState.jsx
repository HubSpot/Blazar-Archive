import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { routerShape } from 'react-router';

import PageContainer from '../shared/PageContainer.jsx';
import ModuleList from './ModuleList.jsx';
import BranchStateHeadline from './BranchStateHeadline.jsx';

const BranchState = ({activeModules, inactiveModules, branchId, branchInfo, router, selectModule, deselectModule, selectedModuleId}) => {
  const onBranchSelect = (selectedBranchId) => {
    router.push(`/branchState/branch/${selectedBranchId}`);
  };

  const handleModuleItemClick = (id) => {
    if (id === selectedModuleId) {
      deselectModule();
    } else {
      selectModule(id);
    }
  };

  const title = branchInfo.branch ? `${branchInfo.repository}-${branchInfo.branch}` : 'Branch State';

  return (
      <PageContainer classNames="page-content--branch-state" documentTitle={title}>
        <BranchStateHeadline branchId={branchId} branchInfo={branchInfo} onBranchSelect={onBranchSelect} />
        <section id="active-modules">
          <h2 className="module-list-header">Active modules</h2>
          <ModuleList modules={activeModules} onItemClick={handleModuleItemClick} selectedModuleId={selectedModuleId} />
        </section>
        {!!inactiveModules.size && <section id="inactive-modules">
          <h2 className="module-list-header">Inactive modules</h2>
          <ModuleList modules={inactiveModules} onItemClick={handleModuleItemClick} selectedModuleId={selectedModuleId} />
        </section>}
      </PageContainer>
  );
};

BranchState.propTypes = {
  branchId: PropTypes.number.isRequired,
  branchInfo: PropTypes.object,
  activeModules: ImmutablePropTypes.list,
  inactiveModules: ImmutablePropTypes.list,
  router: routerShape,
  selectModule: PropTypes.func.isRequired,
  deselectModule: PropTypes.func.isRequired,
  selectedModuleId: PropTypes.number
};

export default BranchState;
