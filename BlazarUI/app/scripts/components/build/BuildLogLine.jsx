import React, {Component, PropTypes} from 'react';

class BuildLogLine extends Component {

  render() {
    const offset = `offset-${this.props.offset}`;
    return (
      <p id={offset} className='log-line'>{this.props.text}</p>
    );
  }
}

BuildLogLine.propTypes = {
  text: PropTypes.string,
  offset: PropTypes.number
};

export default BuildLogLine;
