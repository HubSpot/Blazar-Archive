import React, {Component, PropTypes} from 'react';
import ReactZeroClipboard from 'react-zeroclipboard';
import { bindAll } from 'underscore';
import Tooltip from 'react-bootstrap/lib/Tooltip';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger';

class Copyable extends Component {

  constructor() {
    bindAll(this, 'handleClick', 'handleHover');
  }

  handleClick() {
    this.props.click();
    document.getElementById('copy-tooltip-text').innerHTML = 'Copied!';
    document.getElementById('copy-tooltip-text').fixTitle();
  }

  handleHover() {
    this.props.hover();
  }

  render() {
    let tooltip = (
      <Tooltip id="copy-tooltip">
            <span id="copy-tooltip-text">Copy to clipboard</span>
      </Tooltip>
    );

    return (
      <span>
          <ReactZeroClipboard text={this.props.text}>
            <OverlayTrigger placement='bottom' overlay={tooltip}>
              <span onClick={this.handleClick} onMouseOver={this.handleHover}>{this.props.children}</span>
            </OverlayTrigger>
          </ReactZeroClipboard>
      </span>
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
