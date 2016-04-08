# Changelog

## April 7th, 2016 - Blazar URL Scheme Change

Blazar pretty URLs have been replaced with id-based URLs.

### PR

https://github.com/HubSpot/Blazar/pull/152

### Details

Old URL | New URL
----- | -----
`/builds/<hostName>/<orgName>` | `/builds/org/<orgName>`
`/builds/<hostName>/<orgName>/<repoName>` | `/builds/repo/<repoName>`
`/builds/<hostName>/<orgName>/<repoName>/<branchName>` | `/builds/branch/<branchId>`
`/builds/<hostName>/<orgName>/<repoName>/<branchName>/<buildNumber>` | `/builds/branch/<branchId>/build/<buildNumber>`
`/builds/<hostName>/<orgName>/<repoName>/<branchName>/<buildNumber>/<moduleName>` |  `/builds/branch/<branchId>/build/<buildNumber>/module/<moduleName>`

### Rationale

Using pretty URLs brings many advantages: readability, ease of swapping out path params to bring you to the intended page, etc.

Unfortunately, the biggest downside is a huge slowdown for URLs with many path params. For each of these pages we wish to load, we must begin at the global `/state` endpoint that retrieves all builds for every branch in every repo. Then, using the path params, we filter the builds to locate the corresponding ID of the path param. Using the ID, we make another API call, filter again using the next path param, and continue on until we find the data we want. By nature, these API calls must be sequential. Especially for users on the VPN using Blazar (users in Dublin), the time it takes to load a page that, by the end, does not contain all that much information, is too long to make Blazar usable.

For those reasons, we’ve replaced names with IDs where possible in Blazar URLs. With this change, each page usually needs to make a single API call - sometimes, we make two simultaneous API calls. The end result is a massive increase on the **branch build history, repo build, and module build** pages.
