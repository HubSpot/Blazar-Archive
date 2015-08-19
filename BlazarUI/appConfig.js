// Variables to export to the static app

const appConfig = {
  // sidebar polling frequency in ms
  buildsRefresh: 5000,

  appRoot: process.env.BLAZAR_APP_URI || '/blazar/ui',
  // If we have an env variable, gulp is proxying it through /api.
  // Otherwise we'll get it from localStorage later.
  apiRoot: process.env.BLAZAR_API_URL || ''
}

module.exports = appConfig;
