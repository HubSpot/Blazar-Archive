// Variables to export to the static app

const appConfig = {
  // sidebar polling frequency in ms
  buildsRefresh: 10000,

  appRoot: process.env.BLAZAR_APP_URI || '/blazar/ui',
  apiRoot: process.env.BLAZAR_API_URL || ''
}

module.exports = appConfig;
