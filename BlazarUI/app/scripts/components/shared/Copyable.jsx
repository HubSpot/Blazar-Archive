import React, {Component, PropTypes} from 'react';
import ReactZeroClipboard from 'react-zeroclipboard';
import Tooltip from 'react-bootstrap/lib/Tooltip';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger';

const tooltipTextClicked = 'Copied!';

class Copyable extends Component {

  constructor(props) {
    super(props);
    this.handleClick = this.handleClick.bind(this);
    this.state = {
      tooltipText: this.props.tooltip
    };
  }

  componentDidUpdate() {
    if (this.state.tooltipText === tooltipTextClicked) {
      setTimeout( () => {
        this.setState({
          tooltipText: this.props.tooltip
        });
      }, 2000);
    }
  }

  handleClick() {
    this.setState({
      tooltipText: tooltipTextClicked
    });
  }
  
  renderTooltip() {
    return (
      <Tooltip id="copy-tooltip">
        <span id="copy-tooltip-text">{this.state.tooltipText}</span>
      </Tooltip>
    );
  }

  render() {
    return (
      <span>
          <ReactZeroClipboard text={this.props.text}>
            <OverlayTrigger placement='bottom' overlay={this.renderTooltip()}>
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
