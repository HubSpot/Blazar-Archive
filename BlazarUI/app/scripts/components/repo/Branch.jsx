import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import ModulesTable from '../branch/ModulesTable.jsx';
import Icon from '../shared/Icon.jsx';

class Branch extends Component {

  constructor() {

    bindAll(this, 'handleExpandToggleClick');

    this.state = {
      expanded: false
    };
  }

  componentDidMount() {
    if (this.props.branch.branch === 'master') {
      this.setState({
        expanded: true
      });
    }
  }

  getExpandedState() {
    return this.state.expanded ? '' : 'hide';
  }

  getExpandedIcon() {
    return this.state.expanded ? 'minus' : 'plus';
  }

  handleExpandToggleClick() {
    this.setState({
      expanded: !this.state.expanded
    });
  }

  render() {
    let branch = this.props.branch;
    return (
      <div className='repo-branch'>
        <h4 onClick={this.handleExpandToggleClick} className='repo-branch__headline'>
        <Icon classNames='repo-branch__headine-icon' type='fa' name={this.getExpandedIcon()} />
          {branch.branch}
        </h4>
        <div className={this.getExpandedState()}>
          <ModulesTable modules={this.props.branch.modules} />
        </div>
      </div>
    );
  }

}


Branch.propTypes = {
  branch: PropTypes.object.isRequired
};

export default Branch;
