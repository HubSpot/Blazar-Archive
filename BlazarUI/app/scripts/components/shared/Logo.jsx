import React, {Component, PropTypes} from 'react';
import Image from '../shared/Image.jsx';
import Classnames from 'classnames';

class Logo extends Component {

  getRenderedClassNames() {
    return Classnames([
      'logo',
      {crumb: this.props.crumb}
    ]);
  }

  render() {
    const imgPath = `${window.config.staticRoot}/images/blazar-logo.png`;
    return (
      <div className={this.getRenderedClassNames()}>
        <Image classNames="logo-image" src={imgPath} />
      </div>
    );
  }

}

Logo.defaultProps = {
  crumb: true
};

Logo.propTypes = {
  crumb: PropTypes.bool
};

export default Logo;
