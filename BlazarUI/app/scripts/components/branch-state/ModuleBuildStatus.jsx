import React from 'react';
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

const ModuleBuildStatus = ({moduleBuild}) => {
  const state = moduleBuild.get('state');
  switch (state) {
    case ModuleBuildStates.QUEUED:
    case ModuleBuildStates.WAITING_FOR_UPSTREAM_BUILD:
      return <p><Icon name="clock-o" classNames="module-build-icon--info" />Waiting to build...</p>;

    case ModuleBuildStates.LAUNCHING:
      return <p><Icon for="spinner" classNames="module-build-icon--info" /> Starting build...</p>;

    case ModuleBuildStates.IN_PROGRESS:
      return <p><Icon for="spinner" classNames="module-build-icon--info" /> Building now...</p>;

    case ModuleBuildStates.SUCCEEDED:
    case ModuleBuildStates.FAILED: {
      const startTimestamp = moduleBuild.get('startTimestamp');
      const endTimestamp = moduleBuild.get('endTimestamp');
      const startTime = moment(startTimestamp).fromNow();
      const duration = getBuildDuration(startTimestamp, endTimestamp);

      const isSuccessful = state === ModuleBuildStates.SUCCEEDED;
      const buildResult = isSuccessful ? 'Built' : 'Failed';
      const iconClasses = `status-circle status-circle--${isSuccessful ? 'success' : 'failed'}`;
      return <p><span className={iconClasses} />{buildResult} <strong>{startTime}</strong> in <strong>{duration}</strong></p>;
    }

    case ModuleBuildStates.CANCELLED:
      return <p><Icon name="ban" classNames="module-build-icon--warning" /> Cancelled</p>;

    case ModuleBuildStates.SKIPPED:
      return <p>Skipped</p>;

    default:
      return <p>{state}</p>;
  }
};

ModuleBuildStatus.propTypes = {
  moduleBuild: ImmutablePropTypes.map
};

export default ModuleBuildStatus;
