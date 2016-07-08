import BuildStates from './BuildStates';

import keyMirror from 'keymirror';

export const INTER_PROJECT_STATES = [
  BuildStates.SUCCEEDED,
  BuildStates.FAILED,
  BuildStates.CANCELLED,
  BuildStates.IN_PROGRESS,
  BuildStates.QUEUED
];

export const InterProjectBuildTypes = keyMirror({
  UPSTREAM: null,
  DOWNSTREAM: null,
  FAILED: null
});

const INTER_PROJECT_STATE_TO_CLASS_NAMES = {
  SUCCEEDED: 'success',
  FAILED: 'danger',
  CANCELLED: 'warning',
  IN_PROGRESS: 'in-progress',
  QUEUED: ''
};

export function getInterProjectClassName(state) {
  return INTER_PROJECT_STATE_TO_CLASS_NAMES[state];
}
