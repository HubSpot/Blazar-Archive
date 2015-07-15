import React from 'react';

class Label extends React.Component {

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
  children: React.PropTypes.node.isRequired,
  type: React.PropTypes.oneOf(['default', 'primary', 'success', 'info', 'warning', 'danger'])
};

export default Label;
