import React, {Component, PropTypes} from 'react';
import Toggle from 'react-toggle';
import { bindAll } from 'underscore';

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

    return (
      <span>
        <label htmlFor="watching-status">Notify when builds complete</label>
        <Toggle
          id="watching-status"
          defaultChecked={this.state.watching}
          onChange={this.handleWatchingChange} />
      </span>
    );
  }

}

NotificationToggle.propTypes = {
  repo: PropTypes.string.isRequired,
  branch: PropTypes.string.isRequired
};

export default NotificationToggle;
