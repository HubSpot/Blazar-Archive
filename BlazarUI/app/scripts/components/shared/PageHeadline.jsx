import React from 'react';

class PageHeadline extends React.Component{

  render() {
    return (
      <div>
        <h2 className='header-primary'>
          {this.props.headline}
        </h2>
        <h3 className='header-subheader'>
          {this.props.subheadline}
        </h3>
        {this.props.children}
      </div>
    );
  }
}

PageHeadline.propTypes = {
  headline: React.PropTypes.string.isRequired,
  subheadline: React.PropTypes.string,
  children: React.PropTypes.node
};

export default PageHeadline;
