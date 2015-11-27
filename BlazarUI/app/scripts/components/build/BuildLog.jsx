import React, {Component, PropTypes} from 'react';
import $ from 'jquery';
import {humanizeText, buildIsOnDeck, buildIsInactive, timestampFormatted} from '../Helpers';
import {bindAll, extend} from 'underscore';
import ClassNames from 'classnames';
import BuildLogLine from './BuildLogLine.jsx';
import Icon from '../shared/Icon.jsx';
import Loader from '../shared/Loader.jsx';
import BuildStates from '../../constants/BuildStates';

window.$ = $;

const initialState = {
  logExpanded: false,
  isTailing: false,
  fetchingPrevious: false,
  fetchingNext: false
}

const refreshedState = {
  fetchingPrevious: false,
  fetchingNext: false  
}

class BuildLog extends Component {

  constructor(props, context) {
    super(props, context);
    bindAll(this, 'handleScroll', 'toggleLogSize')

    this.state = initialState;
  }

  componentDidMount() {
    $('#log').on('scroll', this.handleScroll);
  }

  componentWillReceiveProps(nextProps) {
    const nextLog = nextProps.log;
    const buildInProgress = nextProps.build.build.state === BuildStates.IN_PROGRESS;
    let stateUpdates = {};

    if (nextProps.log.shouldPoll && !this.state.isTailing && buildInProgress) {
      stateUpdates.isTailing = true
    }

    if (nextProps.positionChange === 'top' && buildInProgress) {
      stateUpdates.isTailing = false;
    }

    if (nextLog) {
      stateUpdates.fetchingNext = nextLog.fetchAction === 'next' && nextLog.maxOffsetLoaded !== nextLog.options.size;  
    }

    this.setState(extend({}, refreshedState, stateUpdates));
  }

  componentDidUpdate() {
    const {log, build, positionChange} = this.props;

    if (positionChange) {
      this.hasChangedPosition = true;
    }

    // ignore any fetched data if we already processed
    if (log.fetchCount > 1 && log.fetchTimestamp === this.state.lastFetchTimestamp) {    
      return;
    }

    if (log.fetchCount === 1) {
      this.scrollId = `offset-${log.currentOffsetLine}`;
    }

    // Store nextScrollId so we can set scroll
    // to the correct position with our requestAnimationFrame
    this.nextScrollId = `offset-${log.currentOffsetLine}`;

    // used navigation buttons
    if (positionChange) {
      if (positionChange === 'top') {
        this.scrollToTop();
      }

      // dont scroll to bottom if we just did and then DidUpdate
      else if (positionChange === 'bottom') {
        this.scrollToBottom();
      }
    }

    // initial fetch or tailing
    else if ((log.fetchCount === 1 && !positionChange && !this.state.haveFetchedOnce) || this.state.isTailing) {
      this.scrollToBottom();
    }

    // updates based on scroll change
    else if (log.fetchCount > 1) {
      if (log.fetchAction === 'previous') {
        this.scrollToOffsetLine();  
      }
    }
  }

  componentWillUnmount() {
    $('#log').off('scroll', this.handleScroll)
  }

  scrollToTop() {
    this.ignoreScrollEvents = true;

    window.requestAnimationFrame(() => {
      $('#log').scrollTop(0);
    });
  }
  
  scrollToBottom() {
    this.ignoreScrollEvents = true;

    window.requestAnimationFrame(() => {
      $('#log').scrollTop($('#log')[0].scrollHeight);
    });  
  }

  scrollToOffsetLine() {
    const buildInProgress = this.props.build.build.state === BuildStates.IN_PROGRESS;
    
    const scrollToEl = document.getElementById(this.scrollId);
    scrollToEl.scrollIntoView();

    this.scrollId = this.nextScrollId;
  }

  handleScroll() {
    const $log = $('#log');
    const log = this.props.log;

    if (this.props.loading) {
      return;
    }

    if (this.ignoreScrollEvents) {
      this.ignoreScrollEvents = false;
      return;
    }

    // `Debounce` on animation requests so we 
    // only do this when the browser is ready for it
    if (this.frameRequest != null) {
      cancelAnimationFrame(this.frameRequest);
    }

    this.frameRequest = window.requestAnimationFrame(() => {
      const scrollTop = $log.scrollTop();
      const scrollDirection = this.pastScrollTop < scrollTop ? 'down' : 'up';
      this.pastScrollTop = scrollTop;
      
      const scrollHeight = $log[0].scrollHeight;
      const contentsHeight = $log.outerHeight();
      const buildInProgress = this.props.build.build.state === BuildStates.IN_PROGRESS;
      
      const bottomScrollBuffer = 1;
      const atBottom = scrollTop >= scrollHeight - contentsHeight - bottomScrollBuffer;
      const atTop = scrollTop === 0;

      const shouldFetchNext = this.props.log.maxOffsetLoaded < this.props.log.options.size;

      // at top of page
      if (atTop && !atBottom && this.props.log.minOffsetLoaded > 0) {
        
        this.setState({
          fetchingPrevious: true,
          haveFetchedOnce: true,
          lastFetchTimestamp: this.props.log.fetchTimestamp
        });

        this.props.fetchPrevious();  
      }

      // at bottom of page.
      else if (atBottom && !atTop && scrollDirection !== 'up') {        
        // check if we should fetchNext or start polling again
        if (buildInProgress && !this.state.isTailing && !shouldFetchNext) {
          this.props.requestPollingStateChange(true);
        }
        // Inactive build or haven't reached the log up to the point
        // where we need to start polling again
        else {
          this.props.fetchNext();
        }
        
      }

      else {
        // stop tailing & polling
        if (buildInProgress && this.state.isTailing) {
          this.setState({
            isTailing: false
          });

          this.props.requestPollingStateChange(false);
        }
      }

    });
  }
  
  getContainerClassNames() {
    return ClassNames([
      'build-log',
      {'expanded': this.state.logExpanded}
    ]);
  }
  
  toggleLogSize() {
    this.setState({
      logExpanded: !this.state.logExpanded
    });
  }
  
  getRenderedSizeToggleIcon() {
    const iconName = this.state.logExpanded ? 'compress' : 'expand';
    
    const renderedClassnames = ClassNames([
      'log-expand-toggle',
      {'log-expand-toggle--expanded' : this.state.logExpanded}
    ]);

    return (
      <span onClick={this.toggleLogSize}>
        <Icon name={iconName} classNames={renderedClassnames} />
      </span>
    );
  }
  
  getFetchNextSpinner(){
    if (this.state.fetchingNext || this.state.isTailing) {
      return (
        <Loader align='left' roomy={true} />
      );
    }
  }
  
  getFetchPreviousSpinner() {
    if (!this.state.fetchingPrevious) {
      return null;
    }
    
    return (
      <Loader align='left' roomy={true} />
    );
  }

  generateLines() {
    const {build, log, error} = this.props;

    if ((this.props.loading || !log.fetchCount) && !error) {
      return (
        <Loader align='left' roomy={true} />
      )
    }
    
    if (build.build.state === BuildStates.LAUNCHING ||build.build.state === BuildStates.QUEUED) {
      return (
        <div>
          <BuildLogLine text='Polling for updates...' />
          <Loader align='left' roomy={true} />
        </div>
      );
    }
    
    else if (log.logLines.length === 0) {
      return (
        <BuildLogLine text='No log available' />
      );
    }

    return log.logLines.map((line, i) => {
      return (
        <BuildLogLine offset={line.offset} text={line.text} key={i} />
      );
    });
  }
  
  render() {
    return (
      <pre id='log' 
        ref='log'
        className={this.getContainerClassNames()}
      >
        {this.getFetchPreviousSpinner()}
        {this.getRenderedSizeToggleIcon()}
        {this.generateLines()}
        {this.getFetchNextSpinner()}
      </pre>
    );
  }
}

BuildLog.propTypes = {
  loading: PropTypes.bool.isRequired,
  fetchNext: PropTypes.func.isRequired,
  fetchPrevious: PropTypes.func.isRequired,
  requestPollingStateChange: PropTypes.func.isRequired,
  navigationChangeRequested: PropTypes.string,
  error: PropTypes.node
};

export default BuildLog;
