import React, {Component, PropTypes, findDOMNode} from 'react';
import $ from 'jquery';
import utf8 from 'utf8';
import {bindAll} from 'underscore';
import {events, humanizeText} from '../Helpers';
import Collapsable from '../shared/Collapsable.jsx';
import Loader from '../shared/Loader.jsx';
import MutedMessage from '../shared/MutedMessage.jsx'
import ScrollTo from '../shared/ScrollTo.jsx';
import BuildStates from '../../constants/BuildStates';

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
    if (nextProps.buildState === BuildStates.IN_PROGRESS) {
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
    let spinner;
    const noBuildLog = this.props.buildState === BuildStates.CANCELLED || this.props.buildState === BuildStates.QUEUED;

    if (this.props.loading) {
      <Loader align='top-center' />
    }

    if (!this.props.log || typeof this.props.log !== 'string' || noBuildLog) {
      return <div />;
    }

    if (this.props.fetchingLog) {
      spinner = (
        <Loader align='left' roomy={true} />
      );

    }

    return (
      <Collapsable 
        header='Build Log'
        initialToggleStateOpen={true}
        disableToggle={true}
      >
        <div className='build-log-container'>
          <pre id='log' 
            ref='log'
            className='build-log' 
            dangerouslySetInnerHTML={this.getLogMarkup()} 
          />
          {spinner}
          <ScrollTo className='build-log-scroll' />
        </div>
      </Collapsable>
    );
  }
}

BuildLog.propTypes = {
  log: PropTypes.string,
  fetchingLog: PropTypes.bool,
  buildState: PropTypes.string,
  isBuilding: PropTypes.bool,
  loading: PropTypes.bool
};

export default BuildLog;
