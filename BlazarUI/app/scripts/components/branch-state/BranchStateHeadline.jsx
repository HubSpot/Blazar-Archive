import React, { Component, PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { connect } from 'react-redux';
import Select from 'react-select';

import { Link } from 'react-router';
import { Button } from 'react-bootstrap';

import PageHeader from '../shared/PageHeader.jsx';
import Icon from '../shared/Icon.jsx';
import Star from '../shared/Star.jsx';

import { loadBranchInfo } from '../../redux-actions/branchActions';
import { loadBranches } from '../../redux-actions/repoActions';
import { showBuildBranchModal } from '../../redux-actions/buildBranchFormActions';


class BranchStateHeadline extends Component {
  componentDidMount() {
    const {branchId} = this.props;
    this.props.loadBranchInfo(branchId);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.branchId !== this.props.branchId) {
      this.props.loadBranchInfo(nextProps.branchId);
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

  renderActions() {
    const {branchId} = this.props;
    const buildSettingsLink = `/settings/branch/${branchId}`;
    return (
      <div className="pull-right">
        <Link to={buildSettingsLink}>
          <Button id="build-settings-button">
            Build settings
          </Button>
        </Link>
        <Button id="build-now-button" bsStyle="primary" onClick={this.props.showBuildBranchModal}>
          Build now
        </Button>
        <Star className="branch-state-headline__star" branchId={this.props.branchId} />
      </div>
    );
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
            {this.renderActions()}
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
  showBuildBranchModal: PropTypes.func.isRequired
};

const mapStateToProps = (state) => {
  return {
    branchesList: state.repo.get('branchesList')
  };
};

const mapDispatchToProps = {
  loadBranchInfo,
  loadBranches,
  showBuildBranchModal
};

export default connect(mapStateToProps, mapDispatchToProps)(BranchStateHeadline);
