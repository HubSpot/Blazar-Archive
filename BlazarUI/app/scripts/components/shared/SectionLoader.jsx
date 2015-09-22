import React, {Component, PropTypes} from 'react';
import Classnames from 'classnames';

class SectionLoader extends Component {

  getRenderedClassNames() {
    return Classnames([
      'section-loader',
      {'roomy': this.props.roomy }
    ]);
  }

  render() {
    return (
      <div className={this.getRenderedClassNames()}></div>
    );
  }

}

SectionLoader.propTypes = {
  roomy: PropTypes.bool
};

export default SectionLoader;

