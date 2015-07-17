import React from 'react';

class Line extends React.Component{
  render() {
    return (
      <div className='log__line'>{this.props.children}</div>
    );

  }
}

Line.propTypes = {
  children: React.PropTypes.node
};

class Log extends React.Component {

  render() {

    return (
      <pre className='log'>
        <Line>No log</Line>
      </pre>
    );
  }

}

export default Log;
