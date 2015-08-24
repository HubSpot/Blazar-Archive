import React, {Component, PropTypes} from 'react';

class PageHeadline extends Component{

  render() {
    return (
      <div>
        <h2 className='header-primary'>
          {this.props.headline}{' '}
          <span className='header-subheader'>
            · {this.props.subheadline}
          </span>
        </h2>
        {this.props.children}
      </div>
    );
  }
}

PageHeadline.propTypes = {
  headline: PropTypes.object.isRequired,
  subheadline: PropTypes.string,
  children: PropTypes.node
};

export default PageHeadline;
