import React, {Component, PropTypes, findDOMNode} from 'react';
import $ from 'jquery';
import Collapsable from '../shared/Collapsable.jsx';
import SectionLoader from '../shared/SectionLoader.jsx';
import utf8 from 'utf8';
import {bindAll} from 'underscore';
import {events} from '../Helpers';

class BuildLog extends Component {

  constructor() {
    bindAll(this, 'handleScroll');
    this.isTailing = true;
  }

  componentDidMount() {
    events.listenTo('scroll', this.handleScroll)
  }

  componentWillUnmount() {
    events.removeListener('scroll', this.handleScroll)
  }

  handleScroll() {
    if ($(window).scrollTop() == $(document).height()-$(window).height()) {
      this.isTailing = true;
    } else {
      this.isTailing = false;
    }
  }
   
  componentDidUpdate(nextProps, nextState) {
    if (nextProps.isBuilding) {
      this.scrollToBottom();
    }
  }

  scrollToBottom() {
    if (this.isTailing) {
      window.scrollTo(0, document.body.scrollHeight);
    }
  }

  getLogMarkup() {
    return {__html: utf8.decode(this.props.log)};
  }

  render() {
    if (this.props.loading) {
      return <SectionLoader />;
    }

    if (!this.props.log || typeof this.props.log !== 'string') {
      return <div />;
    }

    return (
      <Collapsable 
        header='Build Log'
        initialToggleStateOpen={true}
      >
        <pre id='log' 
          ref='log'
          className='build-log' 
          dangerouslySetInnerHTML={this.getLogMarkup()} />
      </Collapsable>
    );
  }
}

BuildLog.propTypes = {
  log: PropTypes.string,
  isBuilding: PropTypes.bool,
  loading: PropTypes.bool
};

export default BuildLog;
