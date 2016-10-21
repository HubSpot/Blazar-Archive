import React, { Component, PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { connect } from 'react-redux';
import Select from 'react-select';

import { Link } from 'react-router';
import { Button } from 'react-bootstrap';

import PageHeader from '../shared/PageHeader.jsx';
import Icon from '../shared/Icon.jsx';
import Star from '../shared/Star.jsx';

import { loadBranches } from '../../redux-actions/repoActions';
import { showBuildBranchModal } from '../../redux-actions/buildBranchFormActions';


class BranchStateHeadline extends Component {
  componentWillReceiveProps(nextProps) {
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

  renderActions() {
    return (
      <div className="branch-state-headline__actions">
        <Button id="build-now-button" bsStyle="primary" onClick={this.props.showBuildBranchModal}>
          Build now
        </Button>
        <Star className="branch-state-headline__star" branchId={this.props.branchId} />
      </div>
    );
  }

  renderBranchSelect() {
    return (
      <div className="branch-state-headline__branch-select">
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
    );
  }

  render() {
    const {branchId, branchInfo: {branch, repository}} = this.props;
    if (!branch) {
      return null;
    }

    return (
      <div>
        <PageHeader>
          {this.renderActions()}
          <div>
            <PageHeader.PageTitle>
              <Icon type="octicon" name="repo" classNames="branch-state-headline__repo-icon" />{repository}
            </PageHeader.PageTitle>
            {this.renderBranchSelect()}
          </div>
        </PageHeader>
        <div className="page-header__sub-header">
          <p className="branch-state-headline__sub-header-links">
            <Link to={`/branches/${branchId}/builds`} className="build-history-link">
              <Icon name="history" /> Branch build history
            </Link>
            <Link to={`/settings/branch/${branchId}`} className="build-settings-link">
              <Icon name="cog" /> Settings
            </Link>
          </p>
        </div>
      </div>
    );
  }
}

BranchStateHeadline.propTypes = {
  branchId: PropTypes.number.isRequired,
  branchInfo: PropTypes.object,
  branchesList: ImmutablePropTypes.list.isRequired,
  loadBranches: PropTypes.func.isRequired,
  onBranchSelect: PropTypes.func.isRequired,
  showBuildBranchModal: PropTypes.func.isRequired
};

const mapStateToProps = (state) => ({
  branchesList: state.repo.get('branchesList')
});

const mapDispatchToProps = {
  loadBranches,
  showBuildBranchModal
};

export default connect(mapStateToProps, mapDispatchToProps)(BranchStateHeadline);
