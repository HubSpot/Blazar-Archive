import React from 'react';

class SectionHeader extends React.Component {

  render() {
    return (
      <h3 className='section-header'>
        {this.props.children}
      </h3>
    );
  }
}

SectionHeader.propTypes = {
  children: React.PropTypes.node
};

export default SectionHeader;
