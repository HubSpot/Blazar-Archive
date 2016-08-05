import React, {Component, PropTypes} from 'react';
import Classnames from 'classnames';

class Loader extends Component {

  getRenderedClassNames() {
    return Classnames([
      'section-loader',
      this.props.className,
      this.props.size,
      this.props.align,
      {'roomy': this.props.roomy}
    ]);
  }

  render() {
    return (
      <div className={this.getRenderedClassNames()}>
        <span className="dot1"></span>
        <span className="dot2"></span>
        <span className="dot3"></span>
      </div>
    );
  }

}

Loader.defaultProps = {
  align: 'center',
  size: 'standard'
};

Loader.propTypes = {
  roomy: PropTypes.bool,
  className: PropTypes.string,
  align: PropTypes.oneOf(['center', 'center-center', 'top-center', 'left', 'right']),
  size: PropTypes.oneOf(['standard', 'mini'])
};

export default Loader;
