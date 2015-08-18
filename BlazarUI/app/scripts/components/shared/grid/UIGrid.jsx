import React, {Component, PropTypes} from 'react';

class UIGrid extends Component {

  getClassName() {
    return `row ${this.props.className}`;
  }

  getContainerClassName() {
    return `container-fluid ui-grid ${this.props.containerClassName}`;
  }

  render() {
    return (
      <div className={this.getContainerClassName()}>
        <div {...this.props} className={this.getClassName()}>
          {this.props.children}
        </div>
      </div>
    );
  }
}

UIGrid.defaultProps = {
  className: '',
  containerClassName: ''
};

UIGrid.propTypes = {
  className: PropTypes.string,
  containerClassName: PropTypes.string,
  children: PropTypes.node
};

export default UIGrid;
