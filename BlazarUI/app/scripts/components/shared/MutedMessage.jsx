import classnames from 'classnames';
import React, {Component, PropTypes} from 'react';

class MutedMessage extends Component {

  getClassNames() {
    return classnames(
       'muted-message',
       this.props.classNames, {
         roomy: this.props.roomy
       }
    );
  }

  render() {
    return (
      <p className={this.getClassNames()}>{this.props.children}</p>
    );
  }

}

MutedMessage.propTypes = {
  classNames: PropTypes.string,
  children: PropTypes.node,
  roomy: PropTypes.bool
};

export default MutedMessage;
