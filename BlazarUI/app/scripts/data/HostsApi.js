/*global config*/
import { findWhere, uniq, sortBy, groupBy } from 'underscore';
import { fromJS } from 'immutable';
import StoredBuilds from './StoredBuilds';

class HostsApi extends StoredBuilds {

  _parse() {
    const grouped = groupBy(this.builds, (build) => {
      return build.gitInfo.host;
    });

    let hosts = [];

    for (let group in grouped) {
      const uniqueOrgs = uniq(grouped[group], (b) => {
        return b.gitInfo.organization;
      });
      
      const uniqueOrgsMapped = uniqueOrgs.map((org) => {
        const {host, organization} = org.gitInfo;

        return {
          name: org.gitInfo.organization,
          blazarPath: `${config.appRoot}/builds/${host}/${organization}`
        };
      });
      
      hosts.push({
        name: group,
        orgs: uniqueOrgsMapped
      });
    }
    
    this.cb(fromJS(hosts));
  }

}

export default HostsApi;
