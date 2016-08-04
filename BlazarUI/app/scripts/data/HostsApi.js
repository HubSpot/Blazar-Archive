import { uniq, groupBy, mapObject } from 'underscore';
import { fromJS } from 'immutable';
import StoredBuilds from './StoredBuilds';

class HostsApi extends StoredBuilds {

  _parse() {
    const grouped = groupBy(this.builds, (build) => {
      return build.gitInfo.host;
    });

    const hosts = [];

    mapObject(grouped, (orgs, group) => {
      const uniqueOrgs = uniq(orgs, (b) => {
        return b.gitInfo.organization;
      });

      const uniqueOrgsMapped = uniqueOrgs.map((org) => {
        const {host, organization} = org.gitInfo;

        return {
          name: org.gitInfo.organization,
          blazarPath: `/builds/${host}/${organization}`
        };
      });

      hosts.push({
        name: group,
        orgs: uniqueOrgsMapped
      });
    });

    this.cb(fromJS(hosts));
  }

}

export default HostsApi;
