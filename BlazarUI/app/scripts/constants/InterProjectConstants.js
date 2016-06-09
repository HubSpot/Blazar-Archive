import BuildStates from './BuildStates';

export const INTER_PROJECT_STATES = [
  BuildStates.SUCCESS,
  BuildStates.FAILED,
  BuildStates.CANCELLED,
  BuildStates.IN_PROGRESS,
  BuildStates.QUEUED
];

const INTER_PROJECT_STATE_TO_CLASS_NAMES = {
  BuildStatesSUCCESS: 'success',
  FAILED: 'danger',
  CANCELLED: 'warning',
  IN_PROGRESS: '',
  QUEUED: ''
};

export function getInterProjectClassName(state) {
  return INTER_PROJECT_STATE_TO_CLASS_NAMES[state];
}
