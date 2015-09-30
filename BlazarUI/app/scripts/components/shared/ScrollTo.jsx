import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import {scrollTo} from '../Helpers';
import Icon from './Icon.jsx';
import ClassNames from 'classnames';

class ScrollTo extends Component {
  
  constructor() {
    this.handleClick = this.handleClick.bind(this);
  }

  getClassNames() {
    return ClassNames([
      'scroll-to',
      this.props.className
    ]);
  }

  handleClick(event) {
    const currentTarget = event.currentTarget
    const direction = event.currentTarget.getAttribute('data-direction');
    scrollTo(direction);
  }

  render() {
    return (
      <div className={this.getClassNames()}>
        <span className='scroll-to-btn' data-direction='top' onClick={this.handleClick}>
          <Icon for='scroll-up' />
        </span>
        <span className='scroll-to-btn' data-direction='bottom' onClick={this.handleClick}>
          <Icon for='scroll-down' />
        </span>
      </div>
    );
  }
}

ScrollTo.propTypes = {
  className: PropTypes.string
}

export default ScrollTo;