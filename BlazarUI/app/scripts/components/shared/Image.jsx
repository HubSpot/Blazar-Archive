import React, {Component, PropTypes} from 'react';

class Image extends Component {

  getStyles() {
    return {
      width: this.props.width,
      height: this.props.height
    };
  }

  render() {
    return (
      <img src={this.props.src} style={this.getStyles()} className={this.props.classNames} />
    );
  }

}

Image.defaultProps = {
  width: 'auto',
  height: 'auto',
  classNames: ''
};

Image.propTypes = {
  src: PropTypes.string.isRequired,
  width: PropTypes.string,
  height: PropTypes.string,
  classNames: PropTypes.string
};

export default Image;
