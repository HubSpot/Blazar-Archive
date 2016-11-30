import moment from 'moment';
import humanizeDuration from 'humanize-duration';
import { Link } from 'react-router';
import BuildStateIcon from '../shared/BuildStateIcon.jsx';

import React, { PropTypes } from 'react';
import BuildStates from '../../constants/BuildStates';

const ModuleRow = ({module}) => {
  const state = module.state;
  let statusText;
  let durationText;
  let extraClass = '';

  if (state === BuildStates.IN_PROGRESS || state === BuildStates.QUEUED || state === BuildStates.LAUNCHING) {
    statusText = 'is building ...';
    durationText = `Started ${moment(module.startTimestamp).fromNow()}`;
  } else if (state === BuildStates.SUCCEEDED) {
    statusText = 'built successfully.';
    durationText = `Finished in ${humanizeDuration(module.endTimestamp - module.startTimestamp, {round: true})}`;
  } else if (state === BuildStates.FAILED) {
    statusText = 'build failed.';
    durationText = `Finished in ${humanizeDuration(module.endTimestamp - module.startTimestamp, {round: true})}`;
  } else if (state === BuildStates.CANCELLED) {
    statusText = 'was cancelled.';
    durationText = `Cancelled after ${humanizeDuration(module.endTimestamp - module.startTimestamp, {round: true})}`;
    extraClass = ' repo-branch-card__expanded-module-row--no-log';
  } else if (state === BuildStates.SKIPPED) {
    statusText = 'was skipped.';
    extraClass = ' repo-branch-card__expanded-module-row--no-log';
  } else if (state === BuildStates.UNSTABLE) {
    statusText = 'build is unstable.';
    extraClass = ' repo-branch-card__expanded-module-row--no-log';
  } else if (state === BuildStates.WAITING_FOR_UPSTREAM_BUILD) {
    statusText = 'is waiting for an upstream build to complete.';
    extraClass = ' repo-branch-card__expanded-module-row--no-log';
  } else if (state === BuildStates.WAITING_FOR_BUILD_SLOT) {
    statusText = 'is waiting for a build slot.';
    extraClass = ' repo-branch-card__expanded-module-row--no-log';
  }

  const rowClassName = `repo-branch-card__expanded-module-row${extraClass}`;

  const innerContent = (
    <div className={rowClassName}>
      <span className="repo-branch-card__expanded-status">
        <div className="repo-branch-card__building-icon-link">
          <BuildStateIcon buildState={state} />
        </div> {module.name} {statusText}
      </span>
      <span className="repo-branch-card__expanded-timestamp">
        {durationText}
      </span>
    </div>
  );

  if (state === BuildStates.SKIPPED || state === BuildStates.CANCELLED) {
    return (
      <div>
        {innerContent}
      </div>
    );
  }

  return (
    <div>
      <Link to={module.blazarPath}>
        {innerContent}
      </Link>
    </div>
  );
};

ModuleRow.propTypes = {
  module: PropTypes.object.isRequired
};

export default ModuleRow;
