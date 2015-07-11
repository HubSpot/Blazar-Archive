import React from 'react';
import SectionHeader from './SectionHeader.jsx'

class PageSection extends React.Component{

  constructor(props, context) {
   super(props);
  }

  render() {
    return (
      <div className='page-section'>
        <SectionHeader>{this.props.headline}</SectionHeader>
        {this.props.children}
      </div>
    );
  }
}


export default PageSection;