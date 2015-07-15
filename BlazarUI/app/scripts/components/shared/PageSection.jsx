import React from 'react';
import SectionHeader from './SectionHeader.jsx';

class PageSection extends React.Component{

  render() {
    let sectionHeader;
    if (this.props.headline) {
      sectionHeader = <SectionHeader>{this.props.headline}</SectionHeader>;
    }
    return (
      <div className='page-section'>
        {sectionHeader}
        {this.props.children}
      </div>
    );
  }
}

PageSection.propTypes = {
  headline: React.PropTypes.string,
  children: React.PropTypes.node
};

export default PageSection;
