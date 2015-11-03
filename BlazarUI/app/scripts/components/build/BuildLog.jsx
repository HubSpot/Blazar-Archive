import React, {Component, PropTypes, findDOMNode} from 'react';
import $ from 'jquery';
import {debounce} from 'underscore';
import {events, humanizeText} from '../Helpers';
import BuildLogLine from './BuildLogLine.jsx';
import Collapsable from '../shared/Collapsable.jsx';
import Loader from '../shared/Loader.jsx';
import MutedMessage from '../shared/MutedMessage.jsx'
import BuildStates from '../../constants/BuildStates';


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
    
    console.log('did Mount, set scrollId: ', this.props.currrentOffsetLine);
    this.scrollToBottom();
    $('#log').on('scroll', this.handleScroll)
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      isPaging: false
    });
  }

  componentDidUpdate(nextProps, nextState) {
    console.log('Component Did Update');
    
    // store next scroll to value so we can set it
    // set it after our requestAnimationFrame is complete
    
    // If navigating using the 'To Top' and 'To Bottom' buttons
    if (this.props.navigating) {
      this.nextScrollId = `offset-${this.props.scrollToOffset}`;
    }
    
    else {
      console.log('setting scrollId to: ', this.props.currrentOffsetLine);
      this.nextScrollId = `offset-${this.props.currrentOffsetLine}`;
    }

    this.scrollToOffset();
    // window.requestAnimationFrame(() => {
    //   this.scrollToOffset();
    // });
    


    // Active builds... --> check position
    // if (nextProps.buildState === BuildStates.IN_PROGRESS) {
  }
  
  componentWillUnmount() {
    $('#log').off('scroll', this.handleScroll)
  }
  
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
      console.log('handle Page up');
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
    
    
    // // at bottom of page
    // if ($(window).scrollTop() === $(document).height() - $(window).height()  ) {
    //   if (this.props.buildState === BuildStates.IN_PROGRESS) {
    //     this.setState({
    //       isTailing: true
    //     });
    //   }
    // } 
    // 
    // // at top of page
    // else if ($(window).scrollTop() <= 30 && !this.state.isPaging) {
    //   this.handlePageUp();
    //   // show loader at top of build log
    //   this.setState({
    //     isPaging: true
    //   });
    // }
    // 
    // else {
    //   if (this.props.buildState === BuildStates.IN_PROGRESS) {
    //     this.setState({
    //       isTailing: false,
    //       isPaging: false
    //     });
    //   }
    // }
    
    
  }
  
  
  
  
  
  scrollToOffset() {
    if (!this.scrollId) {
      return;
    }


    window.requestAnimationFrame(() => {
      console.log('request animation, scroll to: ', this.scrollId);
      document.getElementById(this.scrollId).scrollIntoView();
      this.scrollId = this.nextScrollId;
    });
    
    // const scrolledY = window.scrollY;
    // if (this.props.navigating === 'bottom') {
    //   window.scroll(0, scrolledY)
    // }
    // // hit "To Top" button
    // else if (this.props.navigating === 'top') {
    //   // a little hack to expose the current offset
    //   // line which is hidden behind the fixed header 
    //   const headerHt = 270;
    //   if(scrolledY){
    //     window.scroll(0, scrolledY - headerHt);
    //   }
    // }
    
  }


  checkPosition() {
    if (this.state.isTailing) {
      this.scrollToBottom();
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
  navigating: PropTypes.string,
  scrollToOffset: PropTypes.number,
  fetchingLog: PropTypes.bool,
  position: PropTypes.string,
  pageUp: PropTypes.func,
  buildState: PropTypes.string,
  isBuilding: PropTypes.bool,
  loading: PropTypes.bool
};

export default BuildLog;
