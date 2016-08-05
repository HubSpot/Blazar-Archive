import React, {Component, PropTypes} from 'react';

class Image extends Component {

  getStyles() {
    if (this.props.width || this.props.height) {
      return {
        width: this.props.width || 'auto',
        height: this.props.height || 'auto'
      };
    }

    return {};
  }

  render() {
    return (
      <img src={this.props.src} style={this.getStyles()} className={this.props.classNames} />
    );
  }

}

Image.defaultProps = {
  classNames: ''
};

Image.propTypes = {
  src: PropTypes.string.isRequired,
  width: PropTypes.number,
  height: PropTypes.number,
  classNames: PropTypes.string
};

export default Image;
