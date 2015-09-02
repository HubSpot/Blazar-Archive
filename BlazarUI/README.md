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
$ cd BlazarUI
$ npm install
```

## Development

Build and run the application locally with gulp. By default a local webserver starts at port 5000 and uses livereload. You can define a port with `$ gulp --port 3333`.

```
$ gulp
```

#### Connecting to the API
For local development, to avoid any cross-domain restrictions, the connect-server has a middleware proxy in place. Add your API endpoint URL path to an environment variable named BLAZAR_API_URL and the gulp file will pick it up. You can override your API Root at any time by typing the following into your console: localStorage.set('apiRootOverride', 'http://example/blazar/api')

## Build

Build a minified version of the application in the dist folder.

```
$ gulp build --type prod
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


## Code Structure
The BlazarUI uses React components and Reflux, a library for unidirectional dataflow architecture inspired by ReactJS Flux.

What follows is a run-down of the general structure of the react-components and their related flux actions and stores.

Let's start with the routes.js file. As indicated, this file holds all of our routes. One important component that you will notice called by our routes is the the app.jsx component. You can think of this as our primary template. This component loads the top level navigation, sidebar, and sets in place the rest of the routes. Next you can see the react-router loads a unique `page` component for each route. This is the starting point for all other react-components.  In the `scripts/components` directory each sub-directory represents a page. This directory holds all of its components, starting with the Container component.

The Container component is the first component loaded by the page, conventionally named in the form of `Page` followed by `Container` and is where you will do all of your initial data fetching. It then renders its corresponding sub-component, typically using the same name as the page. For more information about using container components, check out this article: (https://medium.com/@learnreact/container-components-c0e67432e005).

####Style
/components directory
 - Styles specific to react components should be stored within the components directory with the same name as the component.

Layout.styl
 - General structure of the pages

To do..

####Flux actions and stores
To Do

####Collections and models
To Do
