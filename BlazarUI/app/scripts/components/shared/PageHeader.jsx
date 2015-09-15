import React, {Component, PropTypes} from 'react';
import $ from 'jquery';

class PageHeader extends Component {

  componentDidMount() {
    $(document).scroll(function(){
      if ($(this).scrollTop() > 0){
        // animate fixed div to small size:
        $('.page-header').stop().animate({ height: 47 }, 50, 'linear');
      } else {
        //  animate fixed div to original size
        $('.page-header').stop().animate({ height: 94 }, 50, 'linear');
      }
    });
  }

  render() {
    return (
      <div className='page-header'>
        {this.props.children}
      </div>
    );
  }
}

PageHeader.propTypes = {
  children: PropTypes.node
};

export default PageHeader;
