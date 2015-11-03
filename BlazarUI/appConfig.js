// Variables to export to the static app

const appConfig = {
  // sidebar polling frequency in ms
  buildsRefresh: process.env.GLOBAL_REFRESH || 5000,
  
  // byte count when fetching log offsets
  offsetLength: process.env.BLAZAR_OFFSET_LENGTH || 90000,

  appRoot: process.env.BLAZAR_APP_URI || '',
  // If we have an env variable, gulp is proxying it through /api.
  // Otherwise we'll get it from localStorage later.
  apiRoot: process.env.BLAZAR_API_URL || '',

  staticRoot: process.env.BLAZAR_STATIC_ROOT || ''
}

module.exports = appConfig;
