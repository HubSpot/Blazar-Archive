import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import classNames from 'classnames';

import { getClassNameColorModifier } from '../../../constants/ModuleBuildStates';

const ModuleBuildListItemWrapper = ({moduleBuild, className, children}) => {
  const colorModifier = getClassNameColorModifier(moduleBuild.get('state'));
  const classes = classNames(className, 'module-build-item-wrapper', {
    [`module-build-item-wrapper--${colorModifier}`]: colorModifier
  });

  return (
    <div className={classes}>
      {children}
    </div>
  );
};

ModuleBuildListItemWrapper.propTypes = {
  moduleBuild: ImmutablePropTypes.map,
  className: PropTypes.string,
  children: PropTypes.node
};

export default ModuleBuildListItemWrapper;
