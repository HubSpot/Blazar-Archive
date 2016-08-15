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
    this.onMount();
  }

  onMount() {
    this.setState({
      expanded: this.props.initialToggleStateOpen
    });
  }

  getHeaderClassNames() {
    return classNames([
      'collapsable__header',
      this.props.headerClassNames,
      {'collapsable__header--disabled-toggle': this.props.disableToggle}
    ]);
  }

  getIconState() {
    return this.state.expanded ? 'minus' : 'plus';
  }

  handleToggle() {
    if (this.props.disableToggle) {
      return;
    }

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
    return this.state.expanded ? 'show' : 'hide';
  }

  getRenderedIcon() {
    if (this.props.disableToggle) {
      return null;
    }

    return <Icon classNames="collapsable__header-icon" type="fa" name={this.getIconState()} />;
  }

  render() {
    let icon;

    if (this.props.iconName) {
      icon = <Icon type={this.props.iconType} name={this.props.iconName} classNames="icon-roomy" />;
    }

    return (
      <div className={this.getWrapperClassNames()}>
        <h4 onClick={this.handleToggle} className={this.getHeaderClassNames()}>
          {this.getRenderedIcon()}
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
  noBorder: false,
  disableToggle: false
};

Collapsable.propTypes = {
  header: PropTypes.node,
  children: PropTypes.node,
  iconType: PropTypes.oneOf(['fa', 'octicon']),
  iconName: PropTypes.string,
  noBorder: PropTypes.bool,
  updateToggleState: PropTypes.func,
  componentId: PropTypes.number,
  disableToggle: PropTypes.bool,
  initialToggleStateOpen: PropTypes.bool,
  headerClassNames: PropTypes.string
};

export default Collapsable;
