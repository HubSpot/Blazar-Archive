import React, {Component, PropTypes, findDOMNode} from 'react';
import $ from 'jquery';
import {debounce} from 'underscore';
import {events, humanizeText, buildIsOnDeck, buildIsInactive} from '../Helpers';
import BuildLogLine from './BuildLogLine.jsx';
import Collapsable from '../shared/Collapsable.jsx';
import Loader from '../shared/Loader.jsx';
import MutedMessage from '../shared/MutedMessage.jsx'
import BuildStates from '../../constants/BuildStates';

window.$ = $;

class BuildLog extends Component {

  constructor(props, context) {
    super(props, context);    
    this.handleScroll = this.handleScroll.bind(this);
    this.initialState = this.props.buildState;
    this.isTailing = true;
    this.showPagingSpinnerUp = false;
    this.showPagingSpinnerDown = false;
  }

  componentDidMount() {
    $('#log').on('scroll', this.handleScroll);

    if (buildIsOnDeck(this.props.buildState) || this.props.log.logLines.length === 0) {
      return;
    }
    
    this.scrollId = `offset-${this.props.log.currentOffsetLine}`;
    this.scrollToBottom();
  }

  componentWillReceiveProps(nextProps) {
    const buildInProgress = nextProps.buildState === BuildStates.IN_PROGRESS;
    const buildCancelled = nextProps.buildState === BuildStates.CANCELLED;
    const nextLog = nextProps.log;
    const onDeck = buildIsOnDeck(nextProps.buildState);
    
    // still waiting to build or cancelled
    if ((!buildInProgress && onDeck) || buildCancelled) {
      return;
    }

    // fast build: waiting for build and it is complete on first render
    else if (!buildInProgress && !nextLog.positionChange && !nextLog.hasScrolled && nextLog.fetchCount === 1) {
      this.showPagingSpinnerUp = false;
      this.showPagingSpinnerDown = false;
      this.buildCompleteOnLoad = true;
      return;
    }

    // if we use navigation buttons to 
    // navigate down to the bottom of the log
    if (buildInProgress && nextProps.log.fetchCount === 1 && nextProps.log.positionChange === 'bottom') {
      this.isTailing = true;
    }
  
    // if we are at the top - remove the paging spinner
    if (nextProps.log.options.offset < nextProps.log.options.offsetLength) {
      this.showPagingSpinnerUp = false;
      this.showPagingSpinnerDown = true;
    }

    else {
      this.showPagingSpinnerUp = true;
    }  
  
  }

  componentDidUpdate() {
    const buildInProgress = this.props.buildState === BuildStates.IN_PROGRESS;
    const log = this.props.log;

    // Used 'To Top' or 'To Bottom' buttons with Inactive build
    // Or used 'To Bottom' in an In Progress Build
    if (log.positionChange) {

      // prevent our navigationi change from
      // triggering a scroll event
      this.ignoreScrollEvent = true;
      
      if (log.positionChange === 'top') {
        this.isTailing = false;
        this.scrollId = `offset-${log.lastOffsetLine}`;
        this.scrollToTop();
        return;
      }
      else {
        // store the current offest if we start scrolling up again
        this.usedNavigationScrollId = `offset-${log.currentOffsetLine}`;
        this.scrollId = `offset-${log.lastOffsetLine}`;
        this.usedNavigation = true;
        this.isTailing = true;
      }

      this.scrollForNavigatonChange();
    }
    
    // If we were not tailing, but we used the "to bottom button"
    else if (buildInProgress && (this.isTailing || log.fetchCount === 1)) {
      if (log.fetchCount === 1) {
        this.scrollId = `offset-${this.props.log.currentOffsetLine}`;
      }

      this.scrollToBottom();
    }

    // User is tailing or has scrolled up or down
    else {
      // if we have navigated down, and now want to scroll up...
      if (this.usedNavigation) {
        this.scrollId = this.usedNavigationScrollId;
        this.usedNavigation = false;
      }
      // Store nextScrollId so we can set scroll
      // to the correct position with our requestAnimationFrame
      this.nextScrollId = `offset-${this.props.log.currentOffsetLine}`;
      this.scrollToOffset();
    }

  }
  
  componentWillUnmount() {
    $('#log').off('scroll', this.handleScroll)
  }

  handleScroll() {
    if (this.ignoreScrollEvent) {
      this.ignoreScrollEvent = false;
      return;
    }

    const $log = $('#log');
    const log = this.props.log;
    
  
    // `Debounce` on animation requests so we 
    // only do this when the browser is ready for it
    if (this.frameRequest != null) {
      cancelAnimationFrame(this.frameRequest);
    }

    window.requestAnimationFrame(() => {
      const scrollTop = $log.scrollTop();
      const scrollHeight = $log[0].scrollHeight;
      const contentsHeight = $log.outerHeight();
      const buildInProgress = this.props.buildState === BuildStates.IN_PROGRESS;
      
      const bottomScrollBuffer = 1;
      const atBottom = scrollTop >= scrollHeight - contentsHeight - bottomScrollBuffer;
      const atTop = scrollTop === 0;

      // at bottom of page...
      if (atBottom && !atTop) {
        // if we're not tailing, but reached the end of the log, start tailing
        if (buildInProgress && !this.isTailing && log.endOfLogLoaded) {
          this.isTailing = true;
          // Dont fetch if we already are fetching from nav button
          if (!log.hasNavigatedWithButtons) {
            this.props.fetchEndOfLog({poll: true});  
          }
        }

        else {
          this.props.pageLog('down');
          this.pagingDirection = 'down';  
        }
      }
      
      // at top of page
      else if (atTop && !atBottom) {
        this.props.pageLog('up');
        this.pagingDirection = 'up';
      }

      else {
        // stop tailing + polling
        if (this.props.buildState === BuildStates.IN_PROGRESS) {
          if (this.isTailing) {
            this.isTailing = false;
            this.props.shouldPoll(false);
          }
        }
      }

    });

  }
  
  scrollToOffset() {
    
    if (this.buildCompleteOnLoad) {
      this.scrollToBottom();
    }
    
    else if (!this.scrollId) {
      return;
    }

    window.requestAnimationFrame(() => {
      const currentPosition = $('#log').scrollTop();

      if (this.pagingDirection === 'up') {
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
  
  scrollToBottom() {
    window.requestAnimationFrame(() => {
      const lastEl = $('.log-line:eq(-2)')[0]
      lastEl.scrollIntoView();
    });
  }
  
  scrollToTop() {
    window.requestAnimationFrame(() => {
      $('#log')[0].scrollTop = 1;
    });
  }
  
  scrollForNavigatonChange() {
    if (this.props.log.positionChange === 'bottom') {
      $('#log').scrollTop($('#log')[0].scrollHeight);
    }
    else if (this.props.log.positionChange === 'top') {
      $('#log').scrollTop(0);
      
    }
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
    
    else if (this.props.log.logLines.length === 0) {
      return (
        <div>
          <BuildLogLine text='No log available' />
        </div>
      );
    }

    return this.props.log.logLines.map((line, i) => {
      return (
        <BuildLogLine offset={line.offset} text={line.text} key={i} />
      );
    });
  }

  render() {
    let pagingSpinnerUp;
    let pagingSpinnerDown;
    let tailingSpinner;
    let endOfLogMessage;
    
    const buildInProgress = this.props.buildState === BuildStates.IN_PROGRESS;

    if (this.props.loading) {
      <Loader align='top-center' />
    }
    
    if (this.showPagingSpinnerUp) {
      pagingSpinnerUp = (
        <div>
          <Loader align='left' roomy={true} />
        </div>
      )
    }

    if (this.props.log.endOfLogLoaded && !buildInProgress) {
      endOfLogMessage = (
        <p className='log-line log-line-end'>End of log</p>
      )
    }

    if (this.showPagingSpinnerDown && !this.props.log.endOfLogLoaded) {
      pagingSpinnerDown = (
        <div>
          <Loader align='left' roomy={true} />
        </div>
      )
    }
    

    if (this.isTailing && buildInProgress) {
      tailingSpinner = (
        <Loader align='left' roomy={true} />
      );
    }

    return (
      <pre id='log' 
        ref='log'
        className='build-log' 
      >
        {pagingSpinnerUp}
        {this.generateLines()}
        {pagingSpinnerDown}
        {tailingSpinner}
        {endOfLogMessage}
      </pre>
    );
  }
}

BuildLog.propTypes = {
  log: PropTypes.object,
  shouldPoll: PropTypes.func.isRequired,
  fetchStartOfLog: PropTypes.func,
  fetchEndOfLog: PropTypes.func.isRequired,
  positionChange: PropTypes.node,
  fetchingLog: PropTypes.bool,
  position: PropTypes.string,
  pageUp: PropTypes.func,
  buildState: PropTypes.string,
  isBuilding: PropTypes.bool,
  loading: PropTypes.bool
};

export default BuildLog;
