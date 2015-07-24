import React, {Component, PropTypes} from 'react';
import SectionHeader from './SectionHeader.jsx';

class PageSection extends Component{

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
  headline: PropTypes.string,
  children: PropTypes.node
};

export default PageSection;
