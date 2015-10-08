import React, {Component, PropTypes} from 'react';
import Classnames from 'classnames';

class SectionLoader extends Component {

  getRenderedClassNames() {
    return Classnames([
      'section-loader',
      {'roomy': this.props.roomy },
      this.props.align
    ]);
  }

  render() {
    return (
      <div className={this.getRenderedClassNames()}></div>
    );
  }

}

SectionLoader.defaultProps = {
  align: 'center'
};

SectionLoader.propTypes = {
  roomy: PropTypes.bool,
  align: PropTypes.oneOf(['center','left','right'])
};

export default SectionLoader;
