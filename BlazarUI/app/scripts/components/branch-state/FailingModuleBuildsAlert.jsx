import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import Alert from '../shared/Alert.jsx';

const FailingModuleBuildsAlert = ({failingModuleNames}) => {
  const numberOfFailingModules = failingModuleNames.size;
  const heading = numberOfFailingModules === 1 ? 'A module build is failing' :
    `${numberOfFailingModules} module builds are failing`;

  return (
    <Alert className="failing-module-builds" type="danger" titleText={heading}>
      <ul className="failing-module-builds__list">
        {failingModuleNames.map((moduleName) =>
          <li key={moduleName}><a href={`#${moduleName}`}>{moduleName}</a></li>
        )}
      </ul>
    </Alert>
  );
};

FailingModuleBuildsAlert.propTypes = {
  failingModuleNames: ImmutablePropTypes.listOf(PropTypes.string).isRequired
};

export default FailingModuleBuildsAlert;
