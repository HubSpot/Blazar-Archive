import React, {Component, PropTypes} from 'react';
import Toggle from 'react-toggle';
import { bindAll } from 'underscore';
import Tooltip from 'react-bootstrap/lib/Tooltip';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger';

class NotificationToggle extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'handleWatchingChange');
    this.state = { watching: false };
  }

  handleWatchingChange() {
    console.log(this.state.watching);
  }

  render() {
    let tooltip = (
      <Tooltip id="copy-tooltip">
            <span id="copy-tooltip-text">Notify me when builds complete</span>
      </Tooltip>
    );

    return (
      <OverlayTrigger placement='left' overlay={tooltip}>
        <span>
          <Toggle
            id="watching-status"
            defaultChecked={this.state.watching}
            onChange={this.handleWatchingChange} />
        </span>
      </OverlayTrigger>
    );
  }

}

NotificationToggle.propTypes = {
  repo: PropTypes.string.isRequired,
  branch: PropTypes.string.isRequired
};

export default NotificationToggle;
