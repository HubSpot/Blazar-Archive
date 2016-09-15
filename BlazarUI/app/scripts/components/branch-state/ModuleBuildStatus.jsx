import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import moment from 'moment';
import ModuleBuildStates from '../../constants/ModuleBuildStates';
import Icon from '../shared/Icon.jsx';

const getMinutes = (duration) => {
  const minutes = Math.floor(duration.asMinutes());
  switch (minutes) {
    case 0: return '';
    case 1: return '1 minute ';
    default: return `${minutes} minutes`;
  }
};

const getSeconds = (duration) => {
  const seconds = duration.seconds();
  return (seconds === 1) ? '1 second' : `${seconds} seconds`;
};

const getBuildDuration = (startTimestamp, endTimestamp) => {
  const duration = moment.duration(endTimestamp - startTimestamp);
  return getMinutes(duration) + getSeconds(duration);
};

const getIcon = (moduleBuildState) => {
  switch (moduleBuildState) {
    case ModuleBuildStates.QUEUED:
    case ModuleBuildStates.WAITING_FOR_UPSTREAM_BUILD:
      return <Icon name="clock-o" classNames="module-build-icon--info" />;

    case ModuleBuildStates.LAUNCHING:
    case ModuleBuildStates.IN_PROGRESS:
      return <Icon for="spinner" classNames="module-build-icon--info" />;

    case ModuleBuildStates.SUCCEEDED:
      return <span className="status-circle status-circle--success" />;
    case ModuleBuildStates.FAILED:
      return <span className="status-circle status-circle--failed" />;

    case ModuleBuildStates.CANCELLED:
      return <Icon name="ban" classNames="module-build-icon--warning" />;

    default:
      return null;
  }
};

const getStatusMessage = (moduleBuild) => {
  const state = moduleBuild.get('state');

  switch (state) {
    case ModuleBuildStates.QUEUED:
    case ModuleBuildStates.WAITING_FOR_UPSTREAM_BUILD:
      return 'Waiting to build...';

    case ModuleBuildStates.LAUNCHING:
      return 'Starting build...';

    case ModuleBuildStates.IN_PROGRESS:
      return 'Building now...';

    case ModuleBuildStates.SUCCEEDED:
    case ModuleBuildStates.FAILED: {
      const startTimestamp = moduleBuild.get('startTimestamp');
      const endTimestamp = moduleBuild.get('endTimestamp');
      const startTime = moment(startTimestamp).fromNow();
      const duration = getBuildDuration(startTimestamp, endTimestamp);

      const isSuccessful = state === ModuleBuildStates.SUCCEEDED;
      const buildResult = isSuccessful ? 'Built' : 'Failed';

      return <span>{buildResult} <strong>{startTime}</strong> in <strong>{duration}</strong></span>;
    }

    case ModuleBuildStates.CANCELLED:
      return <em>Cancelled</em>;

    case ModuleBuildStates.SKIPPED:
      return <em>Skipped</em>;

    default:
      return state;
  }
};

const ModuleBuildStatus = ({moduleBuild, noIcon}) => {
  const message = getStatusMessage(moduleBuild);

  return noIcon ? <p className="module-build-status">{message}</p> :
    <p className="module-build-status">{getIcon(moduleBuild.get('state'))} {message}</p>;
};

ModuleBuildStatus.propTypes = {
  moduleBuild: ImmutablePropTypes.map,
  noIcon: PropTypes.bool
};

ModuleBuildStatus.defaultProps = {
  noIcon: false
};

export default ModuleBuildStatus;
