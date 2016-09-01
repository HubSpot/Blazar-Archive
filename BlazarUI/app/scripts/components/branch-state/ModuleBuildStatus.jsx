import React from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import moment from 'moment';
import ModuleBuildStates from '../../constants/ModuleBuildStates';

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
      return <p>Waiting for build to start...</p>;

    case ModuleBuildStates.LAUNCHING:
      return <p>Starting build...</p>;

    case ModuleBuildStates.IN_PROGRESS:
      return <p>Building now...</p>;

    case ModuleBuildStates.SUCCEEDED:
    case ModuleBuildStates.FAILED: {
      const startTimestamp = moduleBuild.get('startTimestamp');
      const endTimestamp = moduleBuild.get('endTimestamp');
      const startTime = moment(startTimestamp).fromNow();
      const duration = getBuildDuration(startTimestamp, endTimestamp);

      const isSuccessful = state === ModuleBuildStates.SUCCEEDED;
      const buildResult = isSuccessful ? 'Built' : 'Failed';
      const className = isSuccessful ? 'module-build-status--success' : 'module-build-status--failed';
      return <p><span className={className} />{buildResult} <strong>{startTime}</strong> in <strong>{duration}</strong></p>;
    }

    case ModuleBuildStates.CANCELLED:
      return <p>Cancelled</p>;

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
