import React, {Component, PropTypes} from 'react';
import {Button} from 'react-bootstrap';
import {bindAll, contains} from 'underscore';
import classNames from 'classnames';

import Icon from '../Icon.jsx';
import CancelBuildModal from './CancelBuildModal.jsx';
import RepoBuildActions from '../../../actions/repoBuildActions';
import FINAL_BUILD_STATES from '../../../constants/finalBuildStates';

class CancelBuildButton extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'showConfirmCancelModal', 'hideConfirmCancelModal', 'handleConfirmCancelBuild');
    this.state = {
      cancelling: false,
      showModal: false
    };
  }

  showConfirmCancelModal() {
    this.setState({
      showModal: true
    });
  }

  hideConfirmCancelModal() {
    this.setState({
      showModal: false
    });
  }

  handleConfirmCancelBuild() {
    const repoBuildId = this.props.build.id;
    RepoBuildActions.cancelBuild(repoBuildId);
    this.props.onCancel();
    this.setState({
      cancelling: true,
      showModal: false
    });
  }

  renderButton() {
    const {btnSize, btnStyle, btnClassName} = this.props;

    const cancelButtonContent = (this.state.cancelling) ? (
      <span>
        <Icon for="spinner" /> Cancelling
      </span>
    ) : 'Cancel Build';

    return (
      <Button
        bsSize={btnSize}
        bsStyle={btnStyle}
        disabled={this.state.cancelling}
        onClick={this.showConfirmCancelModal}
        className={classNames('cancel-build-button', btnClassName)}
      >
        {cancelButtonContent}
      </Button>
    );
  }

  render() {
    const buildState = this.props.build.state;
    if (!buildState || contains(FINAL_BUILD_STATES, buildState)) {
      return null;
    }

    return (
      <span>
        {this.renderButton()}
        <CancelBuildModal
          show={this.state.showModal}
          onHide={this.hideConfirmCancelModal}
          onConfirm={this.handleConfirmCancelBuild}
          buildNumber={this.props.build.buildNumber}
        />
      </span>
    );
  }
}

CancelBuildButton.propTypes = {
  onCancel: PropTypes.func.isRequired,
  build: PropTypes.object.isRequired,
  btnClassName: PropTypes.string,
  btnStyle: PropTypes.string,
  btnSize: PropTypes.string
};

export default CancelBuildButton;
