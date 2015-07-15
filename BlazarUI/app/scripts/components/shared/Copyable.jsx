import React from 'react';
import ReactZeroClipboard from 'react-zeroclipboard';
import { bindAll } from 'underscore';

class Copyable extends React.Component {

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
        <span onClick={this.handleClick} onMouseOver={this.handleHover}>{this.props.children}</span>
      </ReactZeroClipboard>
    );
  }

}

Copyable.propTypes = {
  children: React.PropTypes.node.isRequired,
  text: React.PropTypes.string.isRequired,
  click: React.PropTypes.func,
  hover: React.PropTypes.func
};

export default Copyable;
