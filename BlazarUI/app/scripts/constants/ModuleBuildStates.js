import keyMirror from 'keymirror';

export default keyMirror({
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
