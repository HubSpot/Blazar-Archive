var gulp = require('gulp');
var path = require('path');
var $ = require('gulp-load-plugins')();
var del = require('del');
var concatCss = require('gulp-concat-css');
var gulpCopy = require('gulp-copy');
var url = require('url');

// config to sent to html as global config variables
var appConfig = require('./appConfig.js');
console.log('APP CONFIG: ', appConfig);

// set variable via $ gulp --type prod
var environment = $.util.env.type || 'staging';
var isProduction = environment === 'prod';
var webpackConfig = require('./webpack.config.js').getConfig(environment);

var port = $.util.env.port || 5000;
var app = 'app/';
var dist = isProduction ? path.resolve(__dirname, '../BlazarService/target/generated-resources/assets') + '/' : 'dist/';

// https://github.com/ai/autoprefixer
var autoprefixerBrowsers = [
  'ie >= 9',
  'ie_mob >= 10',
  'ff >= 30',
  'chrome >= 34',
  'safari >= 6',
  'opera >= 23',
  'ios >= 6',
  'android >= 4.4',
  'bb >= 10'
];

gulp.task('scripts', function() {
  return gulp.src(webpackConfig.entry)
    .pipe($.webpack(webpackConfig))
    .pipe(isProduction ? $.uglifyjs() : $.util.noop())
    .pipe(gulp.dest(dist + 'js/'))
    .pipe($.size({ title : 'js' }))
    .pipe($.connect.reload());
});

gulp.task('lint', function () {
  return gulp.src(['./app/scripts/**/*.jsx'])
    .pipe($.eslint())
    .pipe($.eslint.format())
    .pipe($.eslint.failOnError());
});

// fonts
gulp.task('fonts', function() {
  return gulp.src([
    './node_modules/font-awesome/fonts/fontawesome-webfont.*',
    './bower_components/octicons/octicons/*.{eot,svg,ttf,woff,svg}'
  ]).pipe(gulp.dest(dist + 'css/fonts/'));
});

// preprocess env variables for html
// and copy html from app to dist
gulp.task('html', function() {
  if (isProduction) {
    appConfig.staticRoot = 'static';
  }
  return gulp.src(app + 'index.html')
    .pipe($.preprocess({context: appConfig }))
    .pipe(gulp.dest(dist))
    .pipe($.size({ title : 'html' }))
    .pipe($.connect.reload());
});

gulp.task('vendorStyles', function () {
  var files = [
    './node_modules/bootstrap/dist/css/bootstrap.css',
    './node_modules/font-awesome/css/font-awesome.css',
    './node_modules/react-select/dist/default.css',
    './bower_components/octicons/octicons/octicons.css'
  ];
  return gulp.src(files)
    .pipe(concatCss('vendor.css'))
    .pipe(gulp.dest(dist + 'css'));
});

gulp.task('styles',function(cb) {
  // convert stylus to css
  return gulp.src(app + 'stylus/main.styl')
    .pipe($.stylus({
      // only compress if we are in production
      compress: isProduction,
      // include 'normal' css into main.css
      'include css' : true
    }))
    .pipe($.autoprefixer({browsers: autoprefixerBrowsers}))
    .pipe(gulp.dest(dist + 'css/'))
    .pipe($.size({ title : 'css' }))
    .pipe($.connect.reload());

});

// start server with livereload
gulp.task('serve', function() {
  $.connect.server({
    root: dist,
    port: port,
    livereload: {
      port: 35729
    },
    fallback: dist + 'index.html',
    middleware: function(connect, opt) {
      if (!isProduction){
        /*if (!process.env.BLAZAR_API_URL) {
          console.log('You must first set your API endpoint url to');
          console.log('an environment variable named BLAZAR_API_URL');
          process.exit(1);
        }*/
        if (process.env.BLAZAR_API_URL) {
          appConfig.apiRoot = '/api';
          return [ (function() {
            var proxy = require('proxy-middleware');
            var options = url.parse(process.env.BLAZAR_API_URL);
            options.route = '/api';
            return proxy(options);
          })()]
        }
      }
      return [];
    }
  });
});

// copy images
gulp.task('images', function(cb) {
  return gulp.src(app + 'images/**/*.{png,jpg,jpeg,gif,svg}')
    .pipe($.size({ title : 'images' }))
    .pipe(gulp.dest(dist + 'images/'));
});

// watch styl, html and js file changes
gulp.task('watch', function() {
  gulp.watch(app + 'stylus/**/*.styl', ['styles']);
  gulp.watch(app + 'index.html', ['html']);
  gulp.watch(app + 'scripts/**/*.js', ['scripts', 'lint']);
  gulp.watch(app + 'scripts/**/*.jsx', ['scripts', 'lint']);
});

// remove bundels
gulp.task('clean', function(cb) {
  del([dist], cb);
});

// by default build project and then watch files in order to trigger livereload
gulp.task('default', ['build', 'serve', 'watch']);

// waits until clean is finished then builds the project
gulp.task('build', ['clean'], function(){
  gulp.start(['images', 'html', 'fonts', 'lint', 'scripts', 'vendorStyles', 'styles']);
});
