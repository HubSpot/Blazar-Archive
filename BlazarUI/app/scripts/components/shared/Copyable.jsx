import React, {Component, PropTypes} from 'react';
import ReactZeroClipboard from 'react-zeroclipboard';
import { bindAll } from 'underscore';
import Tooltip from 'react-bootstrap/lib/Tooltip';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger';

const tooltipTextDefault = 'Copy SHA';
const tooltipTextClicked = 'Copied!';

class Copyable extends Component {

  constructor() {
    bindAll(this, 'handleClick');
    this.state = {
      tooltipText: tooltipTextDefault
    };
  }

  componentDidUpdate() {
    if (this.state.tooltipText === tooltipTextClicked) {
      setTimeout( () => {
        this.setState({
          tooltipText: tooltipTextDefault
        });
      }, 2000);
    }
  }

  handleClick() {
    this.setState({
      tooltipText: tooltipTextClicked
    });
  }

  render() {
    let tooltip = (
      <Tooltip id="copy-tooltip">
        <span id="copy-tooltip-text">{this.state.tooltipText}</span>
      </Tooltip>
    );

    return (
      <span>
          <ReactZeroClipboard text={this.props.text}>
            <OverlayTrigger placement='bottom' overlay={tooltip}>
              <span onClick={this.handleClick}>{this.props.children}</span>
            </OverlayTrigger>
          </ReactZeroClipboard>
      </span>
    );
  }

}

Copyable.propTypes = {
  children: PropTypes.node.isRequired,
  text: PropTypes.string.isRequired
};

export default Copyable;
