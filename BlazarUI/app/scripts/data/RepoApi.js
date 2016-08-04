/* global config*/
import Resource from '../services/ResourceProvider';
import { fromJS } from 'immutable';
import humanizeDuration from 'humanize-duration';

function _parse(resp) {
	                    return resp.filter((branchInfo) => {
		                    return branchInfo.active;
	}).map((branchInfo) => {
		                    return {
			                    label: branchInfo.branch,
			                    value: branchInfo.id
		};
	});
}

function fetchBranchesInRepo(repoId, cb) {
	                    const branchesInRepoPromise = new Resource({
		                    url: `${config.apiRoot}/branches/repo/${repoId}`,
		                    type: 'GET'
	}).send();

	                    return branchesInRepoPromise.then((resp) => {
		                    cb(_parse(resp));
	});
}

export default {
	                  fetchBranchesInRepo
};
