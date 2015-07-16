# BlazarUI

Makes use of:
 - [gulp](https://github.com/gulpjs/gulp)
 - [stylus](https://github.com/LearnBoost/stylus)
 - [webpack](https://github.com/webpack/webpack)
 - [React](https://github.com/facebook/react)
 - [React Router](https://github.com/rackt/react-router)
 - [Reflux](https://github.com/spoike/refluxjs)

## Installation

Install dependencies.

```
$ npm install
$ git clone
```

## Development

Build the application and start livereload. By default the webserver starts at port 5000. You can define a port with `$ gulp --port 3333`.

```
$ gulp
```

## Build

Build a minified version of the application in the dist folder.

```
$ gulp build --type production
```

## Testing

We use [jest](http://facebook.github.io/jest/) to test our application. Store test in [app/scripts/\_\_tests__](./app/scripts/__tests__)

```
$ npm test
```

## Javascript

Javascript entry file: `app/scripts/main.js` <br />


## CSS with Stylus

CSS entry file: `app/stylus/main.styl`<br />


###Requirements
* node
* npm
* gulp
