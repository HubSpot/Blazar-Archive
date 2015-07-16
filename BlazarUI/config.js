
module.exports.getApiEndpoint = function(type) {
  var isDev = type === 'development';
  var endpoint;

  if(isDev){
    return endpoint = process.env['BLAZAR_API_ENDPOINT']
  }
  return ''

}
