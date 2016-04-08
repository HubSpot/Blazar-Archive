/* global config */
import React, {Component, PropTypes} from 'react';
import {contains} from 'underscore';
import {Link} from 'react-router';
import $ from 'jquery';
import Select from 'react-select-plus';
import Image from '../shared/Image.jsx'
import {getIsStarredState} from '../Helpers.js';
import {getPathname} from '../Helpers';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Star from '../shared/Star.jsx';
import Icon from '../shared/Icon.jsx';

let initialState = {
  selectedBranch: ''
};

class RepoBuildHeadline extends Component {

  constructor(props, context) {
    super(props, context);

    this.state = initialState;
  }

  componentDidMount() {
    this.replacePlaceholder();
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.branchInfo.branch !== nextProps.branchInfo.branch) {
      this.replacePlaceholder();
    }
  }

  replacePlaceholder() {
    if (this.props.loading || !this.props.branchInfo.branch) {
      setTimeout(() => {
        this.replacePlaceholder();
      }, 50);
      return;
    }

    $('.Select-placeholder')[0].innerHTML = this.props.branchInfo.branch;
  }

  onBranchSelect(selected) {
    const branchLink = `${config.appRoot}/builds/branch/${selected.value}`;
    this.context.router.push(branchLink);
  }

  getFilteredBranches() {
    const {branches, branchInfo} = this.props;

    return branches.filter((branch) => {
      return branch.label !== branchInfo.branch;
    }).sort((a, b) => {
      if (a.label === 'master') {
        return -1;
      }

      else if (b.label === 'master') {
        return 1;
      }

      return a.label.localeCompare(b.label);
    });
  }

  renderShield() {
    if (this.props.loading) {
      return null;
    }

    const imgPath = `${config.apiRoot}/branches/state/${this.props.params.branchId}/shield`;

    return (
      <div className='branch__shield'>
        <Image src={imgPath} />
      </div>
    );
  }
    
  render() {
    if (this.props.loading) {
      return null;
    }

    const {stars, params, currentRepoBuild} = this.props;
    const branchId = parseInt(this.props.params.branchId, 10);

    return (
      <Headline>
        <Star
          className='icon-roomy'
          isStarred={contains(stars, branchId)}
          id={branchId}
        />
        <Icon type="octicon" name="git-branch" classNames="headline-icon" />
        {this.props.branchInfo.repository} - 
        <Select
          className='branch-select-input'
          name='branchSelect'
          placeholder=''
          value={this.state.selectedBranch}
          options={this.getFilteredBranches()}
          onChange={this.onBranchSelect.bind(this)}
          searchable={false}
          clearable={false}
        />
        {this.renderShield()}
      </Headline>
    )
  }
}

RepoBuildHeadline.propTypes = {
  params: PropTypes.object.isRequired,
  branchId: PropTypes.number,
  loading: PropTypes.bool.isRequired,
  branchInfo: PropTypes.object.isRequired
};

RepoBuildHeadline.contextTypes = {
  router: PropTypes.object.isRequired
};

export default RepoBuildHeadline;
