import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { Link } from 'react-router';
import Alert from '../shared/Alert.jsx';

const FailingModuleBuildsAlert = ({failingModuleBuilds}) => {
  const numberOfFailingModules = failingModuleBuilds.size;
  const heading = numberOfFailingModules === 1 ? 'A module build has failed' :
    `${numberOfFailingModules} module builds have failed`;

  return (
    <Alert className="failing-module-builds" type="danger" iconName="exclamation" titleText={heading}>
      <ul className="failing-module-builds__list">
        {failingModuleBuilds.toJS().map(({blazarModuleBuildPath, moduleName, moduleBuildNumber}) =>
          <li key={moduleName}>
            <Link to={blazarModuleBuildPath}>{moduleName} #{moduleBuildNumber}</Link>
          </li>
        )}
      </ul>
    </Alert>
  );
};

FailingModuleBuildsAlert.propTypes = {
  failingModuleBuilds: ImmutablePropTypes.listOf(
    ImmutablePropTypes.contains({
      moduleName: PropTypes.string,
      moduleBuildNumber: PropTypes.number,
      blazarModuleBuildPath: PropTypes.string
    })
  )
};

export default FailingModuleBuildsAlert;
