import React, {Component, PropTypes} from 'react';
import ReactZeroClipboard from 'react-zeroclipboard';
import { bindAll } from 'underscore';

class Copyable extends Component {

  constructor() {
    bindAll(this, 'handleClick', 'handleHover');
  }

  handleClick() {
    this.props.click();
  }

  handleHover() {
    console.log('hovering');
    this.props.hover();
  }

  render() {
    return (
      <ReactZeroClipboard text={this.props.text}>
        <span title="Copy to clipboard" onClick={this.handleClick} onMouseOver={this.handleHover}>{this.props.children}</span>
      </ReactZeroClipboard>
    );
  }

}

Copyable.propTypes = {
  children: PropTypes.node.isRequired,
  text: PropTypes.string.isRequired,
  click: PropTypes.func,
  hover: PropTypes.func
};

export default Copyable;
