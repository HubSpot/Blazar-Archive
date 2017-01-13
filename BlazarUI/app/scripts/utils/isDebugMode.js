/**
 * Check if the user has enabled debug mode for branch state
 * and branch build pages.
 *
 * Makes diagnosing a failing build easier by providing a direct link
 * to the singularity task for each module build.
 */
const isDebugMode = () => window.location.href.indexOf('?debug') > -1;

export default isDebugMode;
