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

const refreshStates = {
  fetchingNext: false,
  fetchingPrevious: false
}

class BuildLog extends Component {

  constructor(props, context) {
    super(props, context);
    bindAll(this, 'handleScroll', 'toggleLogSize')

    this.state = {
      logExpanded: false,
      isTailing: false,
      fetchingPrevious: false,
      fetchingNext: false
    }
  }

  componentDidMount() {
    $('#log').on('scroll', this.handleScroll);
  }

  componentWillReceiveProps(nextProps) {  
    const nextLog = nextProps.log;
    let stateUpdates = {};
    
    if (nextLog) {
      stateUpdates.fetchingNext = nextLog.fetchAction === 'next' && nextLog.maxOffsetLoaded !== nextLog.options.size;  
    }

    this.setState(extend(refreshStates, stateUpdates));
  }

  componentDidUpdate() {
    // initial fetch
    if (this.props.log.fetchCount === 1 && !this.props.positionChange && !this.state.haveFetchedOnce) {
      this.scrollToBottom();
    }

    // used navigation buttons
    else if (this.props.positionChange) {
      if (this.props.positionChange === 'top') {
        this.scrollToTop();
      }
      else if (this.props.positionChange === 'bottom') {
        this.scrollToBottom();
      }
    }

    // updates based on scroll change
    else if (this.props.log.fetchCount > 1) {
      if (this.props.log.fetchAction === 'previous') {
        this.scrollToOffsetLine();  
      }
    }
  }

  componentWillUnmount() {
    $('#log').off('scroll', this.handleScroll)
  }

  scrollToTop() {
    this.preventFetch = true;

    window.requestAnimationFrame(() => {
      $('#log').scrollTop(0);
    });
  }
  
  scrollToBottom() {
    this.preventFetch = true;

    window.requestAnimationFrame(() => {
      $('#log').scrollTop($('#log')[0].scrollHeight);
    });  
  }

  
  scrollToOffsetLine() {
    const scrollId = `offset-${this.props.log.pastOffsetLine}`;
    
    if (this.previousScrollId === scrollId) {
      return;
    }

    this.previousScrollId = scrollId;
    const scrollToEl = document.getElementById(scrollId);
    scrollToEl.scrollIntoView();
  }

  handleScroll() {
    const $log = $('#log');
    const log = this.props.log;

    if (this.preventFetch) {
      this.preventFetch = false;
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
      const buildInProgress = this.props.buildState === BuildStates.IN_PROGRESS;
      
      const bottomScrollBuffer = 1;
      const atBottom = scrollTop >= scrollHeight - contentsHeight - bottomScrollBuffer;
      const atTop = scrollTop === 0;
      
      // at top of page
      if (atTop && !atBottom && this.props.log.minOffsetLoaded > 0) {
        this.setState({
          fetchingPrevious: true,
          haveFetchedOnce: true
        });

        this.props.fetchPrevious();  
      }

      // at bottom of page... dont trigger if we are scrolling up
      else if (atBottom && !atTop && scrollDirection !== 'up') {
        this.props.fetchNext(); 
      }

      else {
        // stop tailing + polling
        if (buildInProgress && this.state.isTailing) {
          this.setState({
            isTailing: false
          });
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
    if (!this.state.fetchingNext) {
      return null;
    }

    return (
      <Loader align='left' roomy={true} />
    );
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
    const {build, log} = this.props;

    if (this.props.loading) {
      return (
        <Loader />
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
        <div>
          <BuildLogLine text='No log available' />
        </div>
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
  navigationChangeRequested: PropTypes.string
};

export default BuildLog;
