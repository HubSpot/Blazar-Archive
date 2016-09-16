/* global config */
import React, {Component, PropTypes} from 'react';
import Select from 'react-select';
import Image from '../shared/Image.jsx';
import Headline from '../shared/headline/Headline.jsx';
import Star from '../shared/Star.jsx';
import Icon from '../shared/Icon.jsx';
import SimpleBreadcrumbs from '../shared/SimpleBreadcrumbs.jsx';
import moment from 'moment';

class BranchHeadline extends Component {

  constructor(props, context) {
    super(props, context);

    this.state = {moment: moment()};
    this.onBranchSelect = this.onBranchSelect.bind(this);
  }

  componentDidMount() {
    this.interval = setInterval(this.updateMoment.bind(this), 5000);
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.branchInfo.branch !== nextProps.branchInfo.branch) {
      this.props.refreshBranches();
    }
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  updateMoment() {
    this.setState({
      moment: moment()
    });
  }

  onBranchSelect(selected) {
    if (selected === '') {
      setTimeout(this.forceUpdate.bind(this), 1); // is a hack
      return;
    }

    const branchLink = `/builds/branch/${selected}`;
    this.context.router.push(branchLink);
  }

  getFilteredBranches() {
    const {branchesList, branchInfo} = this.props;

    return branchesList.filter((branch) => {
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

  renderShield() {
    const imgPath = `${config.apiRoot}/branches/state/${this.props.params.branchId}/shield?${this.state.moment}`;

    return (
      <div className="branch__shield">
        <Image src={imgPath} />
      </div>
    );
  }

  render() {
    if (this.props.loading) {
      return null;
    }

    const branchId = parseInt(this.props.params.branchId, 10);

    return (
      <div>
        <SimpleBreadcrumbs
          repo={true}
          {...this.props}
        />
        <Headline className="headline branch-headline">
          <Star className="icon-roomy" branchId={branchId} />
          <Icon type="octicon" name="git-branch" classNames="headline-icon" />
          <Select
            className="branch-select-input"
            name="branchSelect"
            noResultsText="No other branches"
            value={this.props.branchInfo.branch}
            options={this.getFilteredBranches()}
            onChange={this.onBranchSelect}
            searchable={true}
            clearable={false}
            openOnFocus={true}
          />
          {this.renderShield()}
        </Headline>
      </div>
    );
  }
}

BranchHeadline.propTypes = {
  params: PropTypes.object.isRequired,
  branchId: PropTypes.number,
  loading: PropTypes.bool.isRequired,
  branchInfo: PropTypes.object.isRequired,
  refreshBranches: PropTypes.func.isRequired,
  branchesList: PropTypes.array.isRequired
};

BranchHeadline.contextTypes = {
  router: PropTypes.object.isRequired
};

export default BranchHeadline;
