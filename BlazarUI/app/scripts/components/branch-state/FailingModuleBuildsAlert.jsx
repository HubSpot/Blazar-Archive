import React from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { Link } from 'react-router';
import Alert from '../shared/Alert.jsx';

const FailingModuleBuildsAlert = ({failingModuleBuildBlazarPaths}) => {
  const numberOfFailingModules = failingModuleBuildBlazarPaths.size;
  const heading = numberOfFailingModules === 1 ? 'A module build is failing' :
    `${numberOfFailingModules} module builds are failing`;

  return (
    <Alert className="failing-module-builds" type="danger" iconName="exclamation" titleText={heading}>
      <ul className="failing-module-builds__list">
        {failingModuleBuildBlazarPaths.map((blazarModuleBuildPath, moduleName) =>
          <li key={moduleName}><Link to={blazarModuleBuildPath}>{moduleName}</Link></li>
        ).toArray()}
      </ul>
    </Alert>
  );
};

FailingModuleBuildsAlert.propTypes = {
  failingModuleBuildBlazarPaths: ImmutablePropTypes.map
};

export default FailingModuleBuildsAlert;
