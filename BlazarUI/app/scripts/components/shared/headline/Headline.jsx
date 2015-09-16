import React, {Component, PropTypes} from 'react';

class Headline extends Component{

  render() {
    return (
      <h2 className={`headline ${this.props.className}`}>
        {this.props.children}
      </h2>
    );
  }
}

Headline.propTypes = {
  children: PropTypes.node,
  className: PropTypes.string
};

export default Headline;
