import keyMirror from 'keymirror';

export default keyMirror({
  // Final build states
  SUCCEEDED: null,
  FAILED: null,
  CANCELLED: null,
  SKIPPED: null,

  // Active build states
  WAITING_FOR_UPSTREAM_BUILD: null,
  WAITING_FOR_BUILD_SLOT: null,
  QUEUED: null,
  LAUNCHING: null,
  IN_PROGRESS: null
});
