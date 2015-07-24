import React, {Component, PropTypes} from 'react';

class Label extends Component {

  getClassNames() {
    let labelType = this.props.type || 'default';
    return 'label label-' + labelType;
  }

  render() {
    return (
      <span className={this.getClassNames()}>{this.props.children}</span>
    );
  }

}

Label.propTypes = {
  children: PropTypes.node.isRequired,
  type: PropTypes.oneOf(['default', 'primary', 'success', 'info', 'warning', 'danger'])
};

export default Label;
