import keyMirror from 'keymirror';
import {contains} from 'underscore';

const ModuleBuildStates = keyMirror({
  // waiting states
  QUEUED: null,
  WAITING_FOR_UPSTREAM_BUILD: null,

  // running states
  LAUNCHING: null,
  IN_PROGRESS: null,

  // complete build states
  SUCCEEDED: null,
  CANCELLED: null,
  FAILED: null,
  SKIPPED: null
});

export const getClassNameColorModifier = (state) => {
  switch (state) {
    case ModuleBuildStates.FAILED:
      return 'failed';
    case ModuleBuildStates.SUCCEEDED:
      return 'success';
    case ModuleBuildStates.CANCELLED:
      return 'warning';
    case ModuleBuildStates.SKIPPED:
      return 'muted';
    default:
      return 'info';
  }
};

const COMPLETE_BUILD_STATES = [
  ModuleBuildStates.SUCCEEDED,
  ModuleBuildStates.CANCELLED,
  ModuleBuildStates.FAILED,
  ModuleBuildStates.SKIPPED
];

export const isComplete = (state) => {
  return contains(COMPLETE_BUILD_STATES, state);
};

export default ModuleBuildStates;
