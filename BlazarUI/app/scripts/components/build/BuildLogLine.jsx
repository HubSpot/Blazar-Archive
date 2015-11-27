import React, {Component, PropTypes} from 'react';
import ClassNames from 'classnames';

class BuildLogLine extends Component {

  getClassNames() {
    return ClassNames([
      'log-line',
      {'log-line-emphasis' : this.props.emphasis}
    ])
  }

  render() {
    const offset = `offset-${this.props.offset}`;
    return (
      <p id={offset} className={this.getClassNames()}>{this.props.text}</p>
    );
  }
}

BuildLogLine.propTypes = {
  text: PropTypes.string,
  offset: PropTypes.number,
  emphasis: PropTypes.bool
};

export default BuildLogLine;
