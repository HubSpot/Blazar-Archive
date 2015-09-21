import React, { Component, PropTypes } from 'react';
import { bindAll } from 'underscore';
import Icon from './Icon.jsx';
import classNames from 'classnames';

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

  getHeaderClassNames() {
    return 'collapsable__header ' + this.props.headerClassNames;
  }

  getIconState() {
    return this.state.expanded ? 'minus' : 'plus';
  }

  handleToggle() {    
    if (this.props.updateToggleState) {
      this.props.updateToggleState(this.props.header);
    }

    this.setState({
      expanded: !this.state.expanded
    });
  }

  getWrapperClassNames() {
    return classNames([
      'collapsable',
      {'no-border': this.props.noBorder }
    ]);
  }

  getInnerClassNames() {
    let classNames = this.state.expanded ? 'show' : 'hide';
    return classNames;
  }

  render() {
    let icon;
    const branch = this.props.branch;

    if (this.props.iconName) {
      icon = <Icon type={this.props.iconType} name={this.props.iconName} classNames="icon-roomy" />
    }
    return (
      <div className={this.getWrapperClassNames()}>
        <h4 onClick={this.handleToggle} className={this.getHeaderClassNames()}>
          <Icon classNames='collapsable__header-icon' type='fa' name={this.getIconState()} />
          {icon}
          {this.props.header}
        </h4>
        <div className={this.getInnerClassNames()}>
          {this.props.children}
        </div>
      </div>
    );
  }

}

Collapsable.defaultProps = {
  headerClassNames: '',
  initialToggleStateOpen: false,
  noBorder: false
};

Collapsable.propTypes = {
  header: PropTypes.node,
  children: PropTypes.node,
  iconType: PropTypes.oneOf(['fa','octicon']),
  iconName: PropTypes.string,
  noBorder: PropTypes.bool,
  updateToggleState: PropTypes.func,
  componentId: PropTypes.number
};

export default Collapsable;
