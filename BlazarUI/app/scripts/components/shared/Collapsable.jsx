import React, { Component, PropTypes } from 'react';
import { bindAll } from 'underscore';
import Icon from './Icon.jsx';

class Collapsable extends Component {

  constructor() {
    bindAll(this, 'handleToggle');

    this.state = {
      expanded: false
    };
  }

  componentDidMount() {
    this.setState({
      expanded: this.props.initialToggleStateOpen
    });
  }

  getIconState() {
    return this.state.expanded ? 'minus' : 'plus';
  }

  handleToggle() {
    this.setState({
      expanded: !this.state.expanded
    });
  }

  getCollapsableState() {
    return this.state.expanded ? 'show' : 'hide';
  }

  render() {
    let icon;
    const branch = this.props.branch;

    if (this.props.iconName) {
      icon = <Icon type={this.props.iconType} name={this.props.iconName} classNames="icon-roomy" />
    }
    return (
      <div className='collapsable'>
        <h4 onClick={this.handleToggle} className='collapsable__header'>
          <Icon classNames='collapsable__header-icon' type='fa' name={this.getIconState()} />
          {icon}
          {this.props.header}
        </h4>
        <div className={this.getCollapsableState()}>
          {this.props.children}
        </div>
      </div>
    );
  }

}

Collapsable.defaultProps = {
  initialToggleStateOpen: false
};

Collapsable.propTypes = {
  header: PropTypes.string,
  children: PropTypes.node,
  iconType: PropTypes.oneOf(['fa','octicon']),
  iconName: PropTypes.string
};

export default Collapsable;
