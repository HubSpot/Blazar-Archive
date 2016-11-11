import React, { Component, PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { connect } from 'react-redux';
import Select from 'react-select';

import { Link } from 'react-router';
import { Button } from 'react-bootstrap';

import PageHeader from '../shared/PageHeader.jsx';
import Icon from '../shared/Icon.jsx';
import Star from '../shared/Star.jsx';

import { showBuildBranchModal } from '../../redux-actions/buildBranchFormActions';
import { getBranchesInRepository } from '../../selectors';

class BranchStateHeadline extends Component {
  getFilteredBranchOptions() {
    const {branchesInRepository, branchInfo} = this.props;

    return branchesInRepository.toJS()
      .filter((branch) => branch.branch !== branchInfo.branch)
      .map((branch) => ({
        label: branch.branch,
        value: branch.id
      }))
      .sort((a, b) => {
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
    // hack: set the key to force react to reinitialize the select component
    // each time the value is changed. this prevents the select input from
    // staying focused and having the focused styling after the value is changed
    const value = this.props.branchInfo.branch;
    return (
      <div className="branch-state-headline__branch-select">
        <Icon type="octicon" name="git-branch" classNames="headline-icon" />
        <Select
          key={value}
          className="branch-select-input"
          name="branchSelect"
          noResultsText="No other branches"
          value={value}
          options={this.getFilteredBranchOptions()}
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
    const {branchId, branchInfo} = this.props;
    if (!branchInfo) {
      return null;
    }

    return (
      <div>
        <PageHeader>
          {this.renderActions()}
          <div>
            <PageHeader.PageTitle>
              <Icon type="octicon" name="repo" classNames="branch-state-headline__repo-icon" />{branchInfo.repository}
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
  branchesInRepository: ImmutablePropTypes.set.isRequired,
  onBranchSelect: PropTypes.func.isRequired,
  showBuildBranchModal: PropTypes.func.isRequired
};

const mapStateToProps = (state) => ({
  branchesInRepository: getBranchesInRepository(state)
});

const mapDispatchToProps = {
  showBuildBranchModal
};

export default connect(mapStateToProps, mapDispatchToProps)(BranchStateHeadline);
