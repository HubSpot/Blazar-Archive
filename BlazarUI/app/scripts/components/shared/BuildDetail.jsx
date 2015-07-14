import React from 'react';

class LastBuild extends React.Component{

  constructor(props, context) {
   super(props);
  }

  render() {
    return (
      <div className='last-build last-build--bad'>
        <p> #28 - 2 days, 22 hr ago. Ran for 7 min 34 sec. </p>
        <p>Commit <a href="#">6da672d</a></p>
        <pre className='commit'>[mavin-release-plugin] prepare for the next development iteration</pre>
        <p>Started by Github push by ssalinas</p>
      </div>
    );
  }

}


export default LastBuild;