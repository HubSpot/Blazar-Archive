import React from 'react';
import SectionHeader from './SectionHeader.jsx'

class PageSection extends React.Component{

  constructor(props, context) {
   super(props);
  }

  render() {
    if(this.props.headline){
      var sectionHeader = <SectionHeader>{this.props.headline}</SectionHeader>
    }
    return (
      <div className='page-section'>
        {sectionHeader}
        {this.props.children}
      </div>
    );
  }
}


export default PageSection;