import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';

class EmptyMessage extends Component {
  
  getClassNames() {
    return classNames([
      'empty-message',
      {'simple': this.props.simple},
      {'no-border': this.props.noBorder}
    ])
  }

  render() {
    return (
      <div className={this.getClassNames()}>
        {this.props.children}
      </div>
    )
  }

};

EmptyMessage.propTypes = {
  children: PropTypes.node,
  simple: PropTypes.bool,
  noBorder: PropTypes.bool
};

export default EmptyMessage;
