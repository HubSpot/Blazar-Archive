import React, {Component, PropTypes, findDOMNode} from 'react';
import $ from 'jquery';
import {debounce} from 'underscore';
import {events, humanizeText} from '../Helpers';
import BuildLogLine from './BuildLogLine.jsx';
import Collapsable from '../shared/Collapsable.jsx';
import Loader from '../shared/Loader.jsx';
import MutedMessage from '../shared/MutedMessage.jsx'
import BuildStates from '../../constants/BuildStates';

window.$ = $






class BuildLog extends Component {

  constructor(props, context) {
    super(props, context);    
    this.handleScroll = this.handleScroll.bind(this);
    this.scrollId = 'offset-0';

    this.state = {
      isTailing: this.props.fetchingLog,
      isPaging: false,
      hasPaged: false
    }
  }
  
  componentDidMount() {
    this.scrollId = `offset-${this.props.currrentOffsetLine}`;
    this.scrollToBottom();
    $('#log').on('scroll', this.handleScroll)
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      isPaging: false
    });
  }

  componentDidUpdate(nextProps, nextState) {
    // If positionChange using the 'To Top' and 'To Bottom' buttons
    if (this.props.positionChange) {
      this.scrollId = `offset-${this.props.scrollToOffset}`;
    }
    // store next scroll to value so we can set it
    // set it after our requestAnimationFrame is complete  
    else {
      this.nextScrollId = `offset-${this.props.currrentOffsetLine}`;
    }
    // positionChange using 'To Top' and 'To Bottom' buttons
    // Move to separate method?
    if (this.props.positionChange) {
      this.scrollForNavigatonChange();
    }
    else {
      this.scrollToOffset();
    }
    
    // To Do:
    // Handle active builds
    // if (nextProps.buildState === BuildStates.IN_PROGRESS) {
  }
  
  componentWillUnmount() {
    $('#log').off('scroll', this.handleScroll)
  }
  

  // checkPosition() {
  //   if (this.state.isTailing) {
  //     this.scrollToBottom();
  //   }
  // }
  
  handlePageUp() {
    if (!this.state.hasPaged) {
      this.props.pageUp();
    }
  }

  handleScroll() {  
    const logPosition = $('#log').scrollTop();
    const logHeight = $('#log')[0].scrollHeight - 240
    
    if (logPosition === logHeight ) {
      if (this.props.buildState === BuildStates.IN_PROGRESS) {
        this.setState({
          isTailing: true
        });
      }
    } 
    // at top of page
    else if ( logPosition <= 30 && !this.state.isPaging) {
      this.handlePageUp();
        this.setState({
          isPaging: true
        });
    }
    
    else {
      if (this.props.buildState === BuildStates.IN_PROGRESS) {
        this.setState({
          isTailing: false
        });
      }
    }
    
  }
  
  scrollToOffset() {
    if (!this.scrollId) {
      return;
    }

    window.requestAnimationFrame(() => {
      document.getElementById(this.scrollId).scrollIntoView();
      this.scrollId = this.nextScrollId;
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
    return this.props.log.map((line, i) => {
      return (
        <BuildLogLine offset={line.offset} text={line.text} key={i} />
      );
    });
  }

  render() {
    let spinner;
    const noBuildLog = this.props.buildState === BuildStates.CANCELLED || this.props.buildState === BuildStates.QUEUED;

    if (this.props.loading) {
      <Loader align='top-center' />
    }

    if (!this.props.log || noBuildLog) {
      return null;
    }

    if (this.state.isTailing && this.props.buildState === BuildStates.IN_PROGRESS) {
      spinner = (
        <Loader align='left' roomy={true} />
      );
    }

    return (
      <pre id='log' 
        ref='log'
        className='build-log' 
      >
        {this.generateLines()}
        {spinner}
      </pre>
    );
  }
}

BuildLog.propTypes = {
  log: PropTypes.array,
  positionChange: PropTypes.string,
  scrollToOffset: PropTypes.number,
  fetchingLog: PropTypes.bool,
  position: PropTypes.string,
  pageUp: PropTypes.func,
  buildState: PropTypes.string,
  isBuilding: PropTypes.bool,
  loading: PropTypes.bool
};

export default BuildLog;
