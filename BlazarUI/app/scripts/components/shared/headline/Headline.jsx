import React, {Component, PropTypes} from 'react';
import ClassNames from 'classnames';

class Headline extends Component{

  getClassNames() {
    return ClassNames([
      'headline',
      this.props.className,
      {'border' : this.props.border}
    ]);
  }

  render() {
    return (
      <h2 className={this.getClassNames()}>
        {this.props.children}
      </h2>
    );
  }
}

Headline.defaultProps = {
  border: false
}

Headline.propTypes = {
  children: PropTypes.node,
  className: PropTypes.string,
  border: PropTypes.bool
};

export default Headline;
