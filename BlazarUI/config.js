
module.exports.getApiEndpoint = function(type) {

  console.log('TYPE: ', type);

  // To do: set this up using prompt and localstorage
  var endpoints = {
    'staging': process.env['BLAZAR_API_STAGING'],
    'qa': process.env['BLAZAR_API_QA'],
    'prod': process.env['BLAZAR_API_PROD']
  };

  return endpoints[type];




}
