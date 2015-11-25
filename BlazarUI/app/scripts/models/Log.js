/*global config*/
import Model from './Model';
import BuildStates from '../constants/BuildStates';
import utf8 from 'utf8';
import {getByteLength} from '../utils/logHelpers';
import {rest, initial, first, last, find, compact} from 'underscore';

class Log extends Model {

  constructor(options) {
    this.init(options);
    super(options);
  }
  
  init(options) {
    this.logLines = [];
    this.fetchCount = 0;
    this.baseRequestLength = config.offsetLength;
    this.requestOffset = this.getMaxOffset(options.size);
    this.lengthOverride = false;
    this.shouldPoll = options.buildState === BuildStates.IN_PROGRESS;
    this.currentOffsetLine = 
    // keep track of if we have loaded the beginning or the end of the log
    this.maxOffsetLoaded = options.size;
    this.minOffsetLoaded = this.requestOffset;
  }
  
  updateLogForNavigationChange(options) {
    this.init(options);
    this.requestOffset = options.position === 'top' ? 0 : this.getMaxOffset(options.size);
    this.fetchAction = options.position;
    this.maxOffsetLoaded = options.position === 'top' ? 0 : options.size;

    return this;
  }

  url() {
    return `${config.apiRoot}/build/${this.options.buildNumber}/log?offset=${this.requestOffset}&length=${this.lengthOverride || this.baseRequestLength}`;
  }

  getMaxOffset(size) {
    return Math.max(size - this.baseRequestLength, 0);
  }
  
  parseInactiveBuild() {
    // first fetch or navigated 'To Bottom'
    if (!this.fetchAction || this.fetchAction === 'bottom') {
      this.handleEndOfLogFetch();
    }

    // Navigated 'To Top'
    else if (this.fetchAction === 'top') {
      this.handleToTopFetch();
    }

    // Scrolling Down
    else if (this.fetchAction === 'next') {
      this.handleNextFetch();
    }

    // Scrolling up
    else if (this.fetchAction === 'previous') {
      this.handlePreviousFetch();
    }

    else {
      console.warn('Parse condition not met. ', this);
    }
  }

  parseActiveBuild() {
    // Scrolling up
    if (this.fetchAction === 'previous') {
      this.handlePreviousFetch();
    }

    else {
      if (this.fetchCount === 1) {
        this.handleFirstActiveFetch();
      }
      
      else {
        this.handleMoreActiveFetch();
      }
    }
  }

  parse() {
    this.fetchCount++;
    this.fetchTimestamp = Date.now();
    this.maxOffsetLoaded = Math.max(this.data.nextOffset, this.maxOffsetLoaded);
    this.minOffsetLoaded = Math.min(this.requestOffset, this.minOffsetLoaded);
    this.buildInProgress = this.options.buildState === BuildStates.IN_PROGRESS;
    this.newLogLines = this.formatLog();

    if (this.buildInProgress) {
      this.parseActiveBuild();
    }

    else {
      this.parseInactiveBuild();
    }  
  }
  
  // active build helpers
  handleFirstActiveFetch() {
    // we already have at least 1 offset
    if (this.requestOffset > 0) {
      // save and remove incomplete first line so we can append it to the 
      // incomplete last line of the next fetch if we continue to scroll up
      this.firstLine = first(this.newLogLines);
      this.newLogLines = rest(this.newLogLines);
    }
    // our log is larger than at least 1 offset
    // e.g. we used the "To Top" button
    else if (this.options.size > this.baseRequestLength) {
      // save and chop off last incomplete line
      this.lastLine = last(this.newLogLines);
      this.newLogLines = initial(this.newLogLines);
    }

    this.logLines = this.newLogLines;
  }

  handleMoreActiveFetch() {
    // if we are tailing/actively polling
    if (this.shouldPoll) {
      // if our next offset is offsetLength, we need to 
      // do some chopping as we may have incomplete lines
      if (this.data.nextOffset - this.data.offset >= this.baseRequestLength) {
        // cutoff last line and save it
        this.lastLine = last(this.newLogLines);
        this.newLogLines = initial(this.newLogLines);
      }
      // as long as our offset is less than offsetLength
      // we dont need to worry about lines being cutoff
      this.logLines = [...this.logLines, ...this.newLogLines];
    }

    // if we are not polling
    else if (!this.shouldPoll) {
      this.parseNextFetch();
    }
  }
    
  handleEndOfLogFetch() {
    // we already have at least 1 offset
    if (this.requestOffset > 0) {
      // save and remove incomplete first line so we can append it to
      // the incomplete last line of the next fetch if we scroll up
      this.firstLine = first(this.newLogLines);
      this.newLogLines = rest(this.newLogLines);
    }

    if (this.options.buildState === BuildStates.IN_PROGRESS) {
      
    }

    this.logLines = [...this.logLines, ...this.newLogLines];
  }

  handleNextFetch() {
    this.parseNextFetch();
  }

  handlePreviousFetch() {
    this.parsePreviousFetch({
      removeFirstLine: this.requestOffset > 0
    });
  }
  
  handleToTopFetch() {
    // save and remove incomplete last line so we can prepend it to
    // the incomplete last line of the next fetch if we scroll up
    this.lastLine = last(this.newLogLines);
    this.newLogLines = initial(this.newLogLines);
    this.logLines = this.newLogLines;
  }

  // Helper for parse, used to manage incomplete lines when scrolling up
  parseNextFetch(options = {}) {
    // save incomplete last line so we can prepend it to the
    // incomplete first line of the next fetch if we continue to scroll down
    const tempLast = last(this.newLogLines);
    // chop off last incomplete line
    this.newLogLines = initial(this.newLogLines);
    // prepend the rest of the first line that was cutoff last fetch
    if (this.lastLine) {
      this.newLogLines[0].text = this.lastLine.text + this.newLogLines[0].text;  
    }
    this.lastLine = tempLast;
    this.logLines = [...this.logLines, ...this.newLogLines];
  }

  // Helper for parse, used to manage incomplete lines when scrolling down
  parsePreviousFetch(options = {}) {
    let {removeFirstLine} = options;
    // save incomplete first line so we can append it to the 
    // incomplete last line of the next fetch if we continue to scroll up
    const tempFirst = first(this.newLogLines);
    // remove the first line which may be incomplete
    // as long as we are not at the beginning of the log
    if (this.requestOffset !== 0 || removeFirstLine) {
      this.newLogLines = rest(this.newLogLines);
    }
    // append extra text to last log line that is incomplete
    this.newLogLines[this.newLogLines.length - 1].text = this.newLogLines[this.newLogLines.length - 1].text + this.firstLine.text;
    // prepend new logLines to existing logLines
    this.firstLine = tempFirst;

    this.logLines = [...this.newLogLines, ...this.logLines];
  }

  fetchPrevious() {
    this.fetchAction = 'previous';

    this.requestOffset = Math.max(this.minOffsetLoaded - this.baseRequestLength, 0);
    // check if we have any overlap if scrolling up
    if (this.requestOffset === 0 && this.minOffsetLoaded < this.baseRequestLength) {
      this.lengthOverride = this.minOffsetLoaded;
    }
    return this.fetch();
  }
  
  fetchNext() {
    this.fetchAction = 'next';
    this.requestOffset = this.data.nextOffset;    
    return this.fetch();
  }

  formatLog() {
    const WHITE_SPACE = /^\s*$/;
    const NEW_LINE = '\n';
    const logData = this.data.data;

    if (logData.match(WHITE_SPACE)) {
      return [];
    }
    
    if (logData.length === 0) {
      return [];
    }

    let offsetRunningTotal = this.requestOffset;

    const splitLines = logData.split(NEW_LINE);
    
    return compact(splitLines.map((line, i) => {
      // store second line because we may chop off the first
      if (i === 1) {
        this.pastOffsetLine = this.currentOffsetLine;
        this.currentOffsetLine = offsetRunningTotal + getByteLength(line);
      }

      if (i === splitLines.length - 1) {
        this.lastOffsetLine = offsetRunningTotal + getByteLength(length);
      }

      return {
        text:  utf8.decode(line),
        offset: offsetRunningTotal += getByteLength(line)
      };
    }));

  }


}

export default Log;
