const config = {
  appRoot: process.env.BLAZAR_APP_URI || '/blazar/ui',
  apiRoot: localStorage.getItem("apiRootOverride") || process.env.BLAZAR_API_URL,
  // sidebar polling frequency in ms
  jobRefresh: 5000
}

export default config;
