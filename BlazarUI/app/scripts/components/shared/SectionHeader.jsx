import React, {Component, PropTypes} from 'react';

class SectionHeader extends Component {

  render() {
    return (
      <h3 className='section-header'>
        {this.props.children}
      </h3>
    );
  }
}

SectionHeader.propTypes = {
  children: PropTypes.node
};

export default SectionHeader;
