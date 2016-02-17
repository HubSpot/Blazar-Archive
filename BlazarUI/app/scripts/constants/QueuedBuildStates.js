import BuildStates from './BuildStates';

export default [
  BuildStates.WAITING_FOR_UPSTREAM_BUILD,
  BuildStates.WAITING_FOR_BUILD_SLOT,
  BuildStates.QUEUED,
  BuildStates.LAUNCHING
];
