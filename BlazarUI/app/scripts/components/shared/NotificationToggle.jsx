import React, {Component, PropTypes} from 'react';
import Toggle from 'react-toggle';
import { bindAll } from 'underscore';
import Tooltip from 'react-bootstrap/lib/Tooltip';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger';
import WatchingProvider from '../WatchingProvider';

class NotificationToggle extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'handleWatchingChange');
    this.state = { watching: WatchingProvider.isWatching({ repo: this.props.repo, branch: this.props.branch }) !== -1 };
  }

  handleWatchingChange() {
    this.setState({watching: !this.state.watching});
    if (!this.state.watching) {
      WatchingProvider.addWatch(this.props.repo, this.props.branch);
    } else {
      WatchingProvider.removeWatch(this.props.repo, this.props.branch);
    }
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
