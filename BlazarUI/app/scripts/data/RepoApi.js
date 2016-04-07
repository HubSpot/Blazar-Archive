/*global config*/
import Resource from '../services/ResourceProvider';
import { fromJS } from 'immutable';
import humanizeDuration from 'humanize-duration';


function fetchBranchesInRepo(params, cb) {
	const branchesInRepoPromise = new Resource({
		url: `${config.apiRoot}/branches/repo/${params.repoId}`,
		type: 'GET'
	}).send();

	return branchesInRepoPromise.then((resp) => {
		cb(resp);
	});
}

export default {
	fetchBranchesInRepo: fetchBranchesInRepo
};