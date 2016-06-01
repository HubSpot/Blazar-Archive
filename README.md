![Blazar](https://github.com/HubSpot/Blazar/blob/master/BlazarUI/app/images/blazar-logo.png?raw=true)

## Overview ##

Blazar is a continuous integration tool designed to integrate with GitHub Enterprise 
and [Singularity](https://github.com/HubSpot/Singularity). It uses GitHub organization webhooks
so that new repositories are automatically discovered. The unit of build in Blazar is a module,
which is an arbitrary subset of a repository. This allows Blazar to only build modules that have
changed when a commit is pushed, rather than building the entire repository every time. It uses 
a pattern similar to Heroku's buildpacks to discover thebuildable units within a repository and 
to define how to do the build for each module.

To run locally, use 
`mvn clean pre-integration-test -Dblazar.port=7199` 
and then go to `http://DOCKER_IP:7199/blazar/v1` in your browser.
