import React from 'react';

class Image extends React.Component {

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
  src: React.PropTypes.string.isRequired,
  width: React.PropTypes.string,
  height: React.PropTypes.string,
  classNames: React.PropTypes.string
};

export default Image;
