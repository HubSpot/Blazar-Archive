import React, {Component, PropTypes} from 'react';
import Icon from '../shared/Icon.jsx';

class SectionLoader extends Component {

  render() {
    return (
      <div className="section-loader">
        <Icon classNames='fa-spin' name='circle-o-notch' type='fa' />
      </div>
    );
  }

}

SectionLoader.propTypes = {
  classNames: PropTypes.string
};

export default SectionLoader;
