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
    // simulate some lines
    let lines = [];
    for (let i = 0; i < 200; i++) {
      lines.push(<Line key={i}>npm@1.4.28 /usr/share/hubspot/jenkins-home/workspace/ben-test/nvm/v0.10.32/lib/node_modules/npm</Line>);
    }

    return (
      <pre className='log'>
        <Line>npm http 200 https://registry.npmjs.org/npm/-/npm-1.4.28.tgz</Line>
        <Line>/usr/share/hubspot/jenkins-home/workspace/ben-test/nvm/v0.10.32/bin/npm -> /usr/share/hubspot/jenkins-home/workspace/ben-test/nvm/v0.10.32/lib/node_modules/npm/bin/npm-cli.js</Line>
        {lines}
      </pre>
    );
  }

}

export default Log;
