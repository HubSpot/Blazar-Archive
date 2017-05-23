import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import moment from 'moment';

import Measure from 'react-measure';
import ModuleBuildStates from '../../../constants/ModuleBuildStates';
import Icon from '../../shared/Icon.jsx';
import ProgressSpinner from '../../shared/ProgressSpinner.jsx';
import BuildDuration from './BuildDuration.jsx';
import BuildDurationStopwatch from './BuildDurationStopwatch.jsx';

const getIcon = (moduleBuildState) => {
  switch (moduleBuildState) {
    case ModuleBuildStates.QUEUED:
    case ModuleBuildStates.WAITING_FOR_UPSTREAM_BUILD:
      return <Icon name="clock-o" classNames="module-build-status__icon module-build-status__icon--info" />;

    case ModuleBuildStates.LAUNCHING:
    case ModuleBuildStates.IN_PROGRESS:
      return <span className="module-build-status__icon"><ProgressSpinner /></span>;

    case ModuleBuildStates.SUCCEEDED:
      return <Icon name="check-circle" classNames="module-build-status__icon module-build-status__icon--success" />;

    case ModuleBuildStates.FAILED:
      return <Icon name="times-circle" classNames="module-build-status__icon module-build-status__icon--danger" />;

    case ModuleBuildStates.CANCELLED:
      return <Icon name="ban" classNames="module-build-status__icon module-build-status__icon--warning" />;

    default:
      return null;
  }
};

const getStatusMessage = (moduleBuild, branchBuildStartTimestamp, abbreviateUnits) => {
  const state = moduleBuild.get('state');

  switch (state) {
    case ModuleBuildStates.QUEUED:
    case ModuleBuildStates.WAITING_FOR_UPSTREAM_BUILD:
      return 'Waiting to build...';

    case ModuleBuildStates.LAUNCHING:
      return 'Starting build...';

    case ModuleBuildStates.IN_PROGRESS: {
      const startTimestamp = moduleBuild.get('startTimestamp');
      const duration = <BuildDurationStopwatch startTimestamp={startTimestamp} abbreviateUnits={abbreviateUnits} />;
      return <span>Building for {duration}</span>;
    }

    case ModuleBuildStates.SUCCEEDED:
    case ModuleBuildStates.FAILED:
    case ModuleBuildStates.CANCELLED: {
      const startTimestamp = moduleBuild.get('startTimestamp');
      const endTimestamp = moduleBuild.get('endTimestamp');
      const endTime = <strong>{moment(endTimestamp).fromNow()}</strong>;
      const duration = <BuildDuration startTimestamp={startTimestamp} endTimestamp={endTimestamp} abbreviateUnits={abbreviateUnits} />;

      if (state === ModuleBuildStates.CANCELLED) {
        return <span>Cancelled <strong>{endTime}</strong> after {duration}</span>;
      }

      const isSuccessful = state === ModuleBuildStates.SUCCEEDED;
      const buildResult = isSuccessful ? 'Built' : 'Failed';
      return <span>{buildResult} <strong>{endTime}</strong> in {duration}</span>;
    }

    case ModuleBuildStates.SKIPPED: {
      const time = branchBuildStartTimestamp && <strong>{moment(branchBuildStartTimestamp).fromNow()}</strong>;
      return <span>Skipped {time}</span>;
    }

    default:
      return state;
  }
};

const ModuleBuildStatus = ({moduleBuild, branchBuildStartTimestamp, noIcon, abbreviateUnitsBreakpoint}) => {
  return (
    <Measure>
      {dimensions => {
        const abbreviateUnits = abbreviateUnitsBreakpoint && dimensions.width < abbreviateUnitsBreakpoint;
        const message = getStatusMessage(moduleBuild, branchBuildStartTimestamp, abbreviateUnits);

        return noIcon ? <p className="module-build-status">{message}</p> :
          <p className="module-build-status">{getIcon(moduleBuild.get('state'))} {message}</p>;
      }}
    </Measure>
  );
};

ModuleBuildStatus.propTypes = {
  moduleBuild: ImmutablePropTypes.map,
  branchBuildStartTimestamp: PropTypes.number,
  noIcon: PropTypes.bool,
  abbreviateUnitsBreakpoint: PropTypes.number
};

ModuleBuildStatus.defaultProps = {
  noIcon: false
};

export default ModuleBuildStatus;
