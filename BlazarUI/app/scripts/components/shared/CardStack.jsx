import React, { Component, PropTypes } from 'react';
import $ from 'jquery';

import Loader from './Loader.jsx';

class CardStack extends Component {
  constructor(props) {
    super(props);
  }

  componentDidMount() {
    if (this.props.onClick) {
      $(window).click(this.props.onClick);
    }
  }

  componentWillUnmount() {
    if (this.props.onClick) {
      $(window).unbind('click');
    }
  }

  render() {
    if (this.props.loading) {
      return (
        <div className='card-stack'>
          <Loader align='center' roomy={true} />
        </div>
      );
    }

    return (
      <div className='card-stack'>
        <div className='card-stack__wrapper'>
          {this.props.header}
          {this.props.children}
        </div>
      </div>
    );
  }
}

CardStack.propTypes = {
  loading: PropTypes.bool.isRequired,
  header: PropTypes.node,
  children: PropTypes.node,
  onClick: PropTypes.func
};

export default CardStack;