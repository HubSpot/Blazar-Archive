import React, {Component, PropTypes, findDOMNode} from 'react';
import $ from 'jquery';
import {debounce} from 'underscore';
import {events, humanizeText, buildIsOnDeck, buildIsInactve} from '../Helpers';
import BuildLogLine from './BuildLogLine.jsx';
import Collapsable from '../shared/Collapsable.jsx';
import Loader from '../shared/Loader.jsx';
import MutedMessage from '../shared/MutedMessage.jsx'
import BuildStates from '../../constants/BuildStates';


class BuildLog extends Component {

  constructor(props, context) {
    super(props, context);    
    this.handleScroll = this.handleScroll.bind(this);
    this.initialState = this.props.buildState;
    this.isTailing = true;
    
    // To do: 
    // can we handle with state... running into issues
    //

    // this.state = {
    //   isTailing: true
    // }
    
  }

  componentDidMount() {
    this.scrollId = `offset-${this.props.currrentOffsetLine}`;
    $('#log').on('scroll', this.handleScroll)

    if (buildIsOnDeck(this.props.buildState) || this.props.log.length === 0) {
      return;
    }
    
    this.scrollToBottom();    
  }

  componentDidUpdate(nextProps, nextState) {
    // Used 'To Top' or 'To Bottom' buttons
    if (this.props.positionChange) {
      if (this.props.positionChange === 'top') {
        this.scrollId = `offset-${this.props.lastOffsetLine}`;  
      }
      else {
        // need to store current offest if we start scrollin gup again
        this.usedNavigationScrollId = `offset-${this.props.currrentOffsetLine}`;
        this.scrollId = `offset-${this.props.lastOffsetLine}`;
        this.usedNavigation = true;
      }

      this.scrollForNavigatonChange();
    }
    
    else if (this.props.buildState === BuildStates.IN_PROGRESS && this.isTailing) {
      this.scrollToBottom();
    }

    // User scrolled up or down
    else {
      // if we have navigated down, and now want to scroll up...
      if (this.usedNavigation) {
        this.scrollId = this.usedNavigationScrollId;
      }
      // Store nextScrollId so we can set scroll
      // to the correct position with our requestAnimationFrame
      this.nextScrollId = `offset-${this.props.currrentOffsetLine}`;
      this.scrollToOffset();
    }

  }

  componentWillUnmount() {
    $('#log').off('scroll', this.handleScroll)
  }

  handleScroll() {
     const $log = $('#log');

    // `Debounce` on animation requests so we 
    // only do this when the browser is ready for it
    if (this.frameRequest != null) {
      cancelAnimationFrame(this.frameRequest);
    }

    window.requestAnimationFrame(() => {
      const scrollTop = $log.scrollTop()
      const scrollHeight = $log[0].scrollHeight
      const contentsHeight = $log.outerHeight()

      const bottomScrollBuffer = 1
      const atBottom = scrollTop >= scrollHeight - contentsHeight - bottomScrollBuffer
      const atTop = scrollTop === 0

      // at bottom og page...
      if (atBottom && !atTop) {
        this.isTailing = true
        this.props.pageLog('down');
        this.pagingDirection = 'down';
      }
      
      // at top of page
      else if (atTop && !atBottom) {
        this.props.pageLog('up');
        this.pagingDirection = 'up';
      }

      else {
        this.isTailing = false;
      }

    });

  }
  
  scrollToOffset() {
    if (!this.scrollId) {
      return;
    }

    window.requestAnimationFrame(() => {
      const currentPosition = $('#log').scrollTop();

      if (this.pagingDirection === 'up') {
        // console.log('scroll into view: ', this.scrollId);
        const scrollToEl = document.getElementById(this.scrollId)
        scrollToEl.scrollIntoView();
      }

      this.scrollId = this.nextScrollId;
      // move the user to the log line that was in view
      // before it hit the buffer to page down.
      if (this.pagingDirection === 'down') {
        $('#log').scrollTop(currentPosition)
      }
    });

  }
  
  scrollForNavigatonChange() {
    if (this.props.positionChange === 'bottom') {
      $('#log').scrollTop($('#log')[0].scrollHeight);
    }
    else if (this.props.positionChange === 'top') {
      $('#log').scrollTop(0);
      
    }
  }
  
  scrollToBottom() {
    window.requestAnimationFrame(() => {
      $('#log').scrollTop($('#log')[0].scrollHeight - 240);
    });
  }

  generateLines() {
    if (this.props.buildState === BuildStates.LAUNCHING || this.props.buildState === BuildStates.QUEUED) {
      return (
        <div>
          <BuildLogLine text='Polling for updates...' />
          <Loader align='left' roomy={true} />
        </div>
      );
    }
    
    else if (this.props.log.length === 0) {
      
      return (
        <div>
          <BuildLogLine text='No log available' />
        </div>
      );
    }

    return this.props.log.map((line, i) => {
      return (
        <BuildLogLine offset={line.offset} text={line.text} key={i} />
      );
    });
  }

  render() {
    let tailingSpinner;
    let pagingUpSpinner;

    if (this.props.loading) {
      <Loader align='top-center' />
    }

    if (this.isTailing && this.props.buildState === BuildStates.IN_PROGRESS) {
      tailingSpinner = (
        <Loader align='left' roomy={true} />
      );
    }

    return (
      <pre id='log' 
        ref='log'
        className='build-log' 
      >
        {this.generateLines()}
        {tailingSpinner}
      </pre>
    );
  }
}

BuildLog.propTypes = {
  log: PropTypes.array,
  positionChange: PropTypes.node,
  fetchingLog: PropTypes.bool,
  position: PropTypes.string,
  pageUp: PropTypes.func,
  buildState: PropTypes.string,
  isBuilding: PropTypes.bool,
  loading: PropTypes.bool
};

export default BuildLog;
