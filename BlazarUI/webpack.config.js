var path = require('path');

module.exports.getConfig = function(type) {

  var isDev = type === 'development';

  var config = {
    entry: './app/scripts/main.js',
    output: {
      path: __dirname,
      filename: 'main.js'
    },
    debug: isDev,
    module: {
      loaders: [
        {
          test: /\.jsx?$/,
          exclude: /node_modules/,
          loader: 'babel-loader'
        },
        {
          test: /\.js$/,
          include: path.resolve(__dirname, 'node_modules/singularityui-tailer'),
          loader: 'babel',
          query: {
            'presets': ['es2015', 'react'],
            'plugins': [
              'transform-object-rest-spread'
            ]
          }
        }
      ]
    }
  };

  if(isDev){
    config.devtool = 'eval';
  }

  return config;
}
