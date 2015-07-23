import React, {Component, PropTypes} from 'react';
import classSet from 'react-classset';

class SectionLoader extends Component {

  getClassNames(provided) {
    return provided + ' ' + classSet({
      'section-loader': true
    });
  }

  render() {
    return (
      <span className={this.getClassNames()}></span>
    );
  }

}

SectionLoader.propTypes = {
  classNames: PropTypes.string
};

export default SectionLoader;
