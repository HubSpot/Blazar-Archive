// Variables to export to the static app

const appConfig = {
  // sidebar polling frequency in ms
  buildsRefresh: process.env.GLOBAL_REFRESH || 10000,

  // active build module polling frequency in ms
  activeBuildModuleRefresh: process.env.ACTIVE_BUILD_MODULE_REFRESH || 1000,

  // active build polling frequency in ms
  activeBuildRefresh: process.env.ACTIVE_BUILD_REFRESH || 2500,

  // active build log polling frequency in ms
  activeBuildLogRefresh: process.env.ACTIVE_BUILD_LOG_REFRESH || 500,

  // byte count when fetching log offsets
  offsetLength: process.env.BLAZAR_OFFSET_LENGTH || 50000,

  appRoot: process.env.BLAZAR_APP_URI || '',
  // If we have an env variable, gulp is proxying it through /api.
  // Otherwise we'll get it from localStorage later.
  apiRoot: process.env.BLAZAR_API_URL || '',

  staticRoot: process.env.BLAZAR_STATIC_ROOT || '',

  usernameCookie: process.env.BLAZAR_USERNAME_COOKIE || '',

  slackBotName: process.env.SLACK_BOT_NAME || '',

  heapToken: process.env.HEAP_TOKEN || false
}

module.exports = appConfig;
