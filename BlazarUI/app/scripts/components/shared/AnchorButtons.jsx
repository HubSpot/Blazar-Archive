import React, {Component, PropTypes} from 'react';
import Button from 'react-bootstrap/lib/Button';
import Helpers from '../ComponentHelpers';
import Icon from './Icon.jsx';

class AnchorButtons extends Component {
  render() {
    return (
      <div className="btn-group-vertical anchor-container" role="group">
        <Button className="anchor-button" onClick={Helpers.scrollToTop} title="Go to top">
          <Icon for="up"></Icon>
        </Button>
        <Button className="anchor-button" onClick={Helpers.scrollToBottom} title="Go to bottom">
          <Icon for="down"></Icon>
        </Button>
      </div>
    );
  }
}

export default AnchorButtons;
