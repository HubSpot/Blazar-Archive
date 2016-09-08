import React, { Component, PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { connect } from 'react-redux';
import Select from 'react-select';
import PageHeader from '../shared/PageHeader.jsx';
import Icon from '../shared/Icon.jsx';
import { loadBranchInfo } from '../../redux-actions/branchActions';
import { loadBranches } from '../../redux-actions/repoActions';
import { loadBranchModuleStates } from '../../redux-actions/branchStateActions';


class BranchStateHeadline extends Component {
  componentDidMount() {
    const {branchId} = this.props;
    this.props.loadBranchInfo(branchId);
    this.props.loadBranchModuleStates(branchId);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.branchId !== this.props.branchId) {
      this.props.loadBranchInfo(nextProps.branchId);
      this.props.loadBranchModuleStates(nextProps.branchId);
      return;
    }

    const repositoryId = nextProps.branchInfo.repositoryId;
    if (repositoryId && (this.props.branchInfo.repositoryId !== repositoryId)) {
      this.props.loadBranches(repositoryId);
    }
  }

  getFilteredBranches() {
    const {branchesList, branchInfo} = this.props;

    return branchesList.toJS().filter((branch) => {
      return branch.label !== branchInfo.branch;
    }).sort((a, b) => {
      if (a.label === 'master') {
        return -1;
      } else if (b.label === 'master') {
        return 1;
      }

      return a.label.localeCompare(b.label);
    });
  }

  render() {
    const {branch, repository} = this.props.branchInfo;
    if (!branch) {
      return null;
    }

    return (
      <div>
        <PageHeader>
          <PageHeader.PageTitle>
            <Icon type="octicon" name="repo" classNames="headline-icon" />{repository}
          </PageHeader.PageTitle>
        </PageHeader>
        <div className="page-header__sub-header">
          <Icon type="octicon" name="git-branch" classNames="headline-icon" />
          <Select
            className="branch-select-input"
            name="branchSelect"
            noResultsText="No other branches"
            value={this.props.branchInfo.branch}
            options={this.getFilteredBranches()}
            onChange={this.props.onBranchSelect}
            searchable={true}
            clearable={false}
            openOnFocus={true}
            autoBlur={true}
          />
        </div>
      </div>
    );
  }
}

BranchStateHeadline.propTypes = {
  branchId: PropTypes.number.isRequired,
  branchInfo: PropTypes.object,
  loadBranchInfo: PropTypes.func.isRequired,
  branchesList: ImmutablePropTypes.list.isRequired,
  loadBranches: PropTypes.func.isRequired,
  onBranchSelect: PropTypes.func.isRequired,
  loadBranchModuleStates: PropTypes.func.isRequired
};

const mapStateToProps = (state) => {
  return {
    branchesList: state.repo.get('branchesList')
  };
};

const mapDispatchToProps = {
  loadBranchInfo,
  loadBranches,
  loadBranchModuleStates
};

export default connect(mapStateToProps, mapDispatchToProps)(BranchStateHeadline);
