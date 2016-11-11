import keyMirror from 'keymirror';
import {contains} from 'underscore';

const BranchBuildStates = keyMirror({
  QUEUED: null,

  // module builds are created here
  // at this point some or none module builds may exist
  LAUNCHING: null,

  IN_PROGRESS: null,

  SUCCEEDED: null,
  CANCELLED: null,
  FAILED: null,
  UNSTABLE: null
});

const COMPLETE_BUILD_STATES = [
  BranchBuildStates.SUCCEEDED,
  BranchBuildStates.CANCELLED,
  BranchBuildStates.FAILED,
  BranchBuildStates.UNSTABLE
];

export const isComplete = (state) => {
  return contains(COMPLETE_BUILD_STATES, state);
};

export default BranchBuildStates;
