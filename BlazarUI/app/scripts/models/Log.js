/*global config*/
import Model from './Model';
import BuildStates from '../constants/BuildStates';
import utf8 from 'utf8';
import {getByteLength} from '../utils/logHelpers';
import {rest, initial, first, last, find, compact} from 'underscore';

class Log extends Model {
  
  constructor(options) {
    this.logLines = [];
    this.fetchCount = 0;
    this.hasFetched = false;
    this.runningOffset = options.startingOffset;
    super(options);
  }

  url() {
    return `${config.apiRoot}/build/${this.options.buildNumber}/log?offset=${this.options.offset}&length=${config.offsetLength}`;
  }

  // Helper for parse, used to manage incomplete lines
  handleLinesOnPageDown(options = {}) {
    let {newLogLines} = options;
    // save incomplete last line so we can prepend it to the
    // incomplete first line of the next fetch if we continue to scroll down
    const tempLast = last(newLogLines);
    // chop off last incomplete line
    newLogLines = initial(newLogLines);
    // prepend the rest of the first line that was cutoff last fetch
    newLogLines[0].text = this.lastLine.text + newLogLines[0].text;
    this.lastLine = tempLast;
    this.logLines = [...this.logLines, ...newLogLines];
  }
  
  // Helper for parse, used to manage incomplete lines
  handleLinesOnPageUp(options = {}) {
    let {newLogLines, removeFirstLine} = options;
    // save incomplete first line so we can append it to the 
    // incomplete last line of the next fetch if we continue to scroll up
    const tempFirst = first(newLogLines);
    // remove the first line which may be incomplete
    // as long as we are not at the beginning of the log
    if (this.options.offset !== 0 || removeFirstLine) {
      newLogLines = rest(newLogLines);
    }
    // append extra text to last log line that is incomplete
    newLogLines[newLogLines.length - 1].text = newLogLines[newLogLines.length - 1].text + this.firstLine.text;
    // prepend new logLines to existing logLines
    this.firstLine = tempFirst;
    this.logLines = [...newLogLines, ...this.logLines];
  }


  parse() {    
    this.fetchCount++;
    let newLogLines = this.formatLog();

    const buildInProgress = this.options.buildState === BuildStates.IN_PROGRESS;

    if (this.options.offset === this.options.lastOffset) {
      this.endOfLogLoaded = true;
    }
    
    if (this.options.offset === 0) {
      this.startOfLogLoaded = true;
    }

    // No new lines, nothing to do
    if (newLogLines.length === 0) {
      this.logLines = this.logLines;
      return;
    }
    
    
    if (buildInProgress) {
      // Initial Log Request
      if (this.fetchCount === 1) {
        // we already have at least 1 offset
        if (this.options.offset > 0) {
          // save and remove incomplete first line so we can append it to the 
          // incomplete last line of the next fetch if we continue to scroll up
          this.firstLine = first(newLogLines);
          newLogLines = rest(newLogLines);
        }
        // our log is larger than at least 1 offset
        // e.g. we used the "To Top" button
        else if (this.options.logSize > config.offsetLength) {
          // save and chop off last incomplete line
          this.lastLine = last(newLogLines);
          newLogLines = initial(newLogLines);
        }

        this.logLines = newLogLines;
      }

      // Not our first request
      else {
        // if we are tailing/actively polling
        if (this.isPolling) {
          // if our next offset is offsetLength, we need to 
          // do some chopping as we may have incomplete lines
          if (this.data.nextOffset - this.data.offset >= config.offsetLength) {
            // cutoff last line and save it
            this.lastLine = last(newLogLines);
            newLogLines = initial(newLogLines);
          }
          // as long as our offset is less than offsetLength
          // we dont need to worry about lines being cutoff
          this.logLines = [...this.logLines, ...newLogLines];
        }
        // if we are not polling
        else if (!this.isPolling) {
          // if we are not polling, a scroll must have triggered this fetch
          if (this.hasScrolled === 'up') {
            this.handleLinesOnPageUp({
              newLogLines: newLogLines,
              removeFirstLine: true
            });
          }
          if (this.hasScrolled === 'down') {
            this.handleLinesOnPageDown({
              newLogLines: newLogLines
            });
          }
        }
        
      }

    }

    else if (!buildInProgress) {    
      // Initial Log Request
      if (this.fetchCount === 1) {
        // Only one offset to ever load
        if (this.options.offset === 0 && this.options.logSize < config.offsetLength) {
          this.logLines = newLogLines;
        }
        // at the first page but we have more offsets to load
        else if (this.options.offset === 0) {
          // save incomplete last line so we can prepend it to the beginning of
          // the first incomplete line on the next fetch if we keep scrolling down
          this.lastLine = last(newLogLines);
          // chop off incomplete last line
          this.logLines = initial(newLogLines)
        }
        // we have more than one offset
        else {
          // save first incomplete line
          this.firstLine = first(newLogLines);
          // chop off first incomplete line
          this.logLines = rest(newLogLines);
        }
      }
      // Not Initial Log Request
      else {
        // Users scrolled up
        if (this.hasScrolled === 'up') {
          this.handleLinesOnPageUp({
            newLogLines: newLogLines
          });
        }
        // User scrolled down
        else if (this.hasScrolled === 'down') {
          this.handleLinesOnPageDown({
            newLogLines: newLogLines
          });
        }
      }
    
    }
    
  }
  
  // Update our fetch offset
  pageLog(hasScrolled) {
    this.isPaging = true;
    this.previousOffset = this.options.offset;

    if (hasScrolled === 'up') {
      // Builds In Progresse
      if (this.options.buildState === BuildStates.IN_PROGRESS) {
        this.options.offset = Math.max(this.runningOffset - config.offsetLength, 0);
        this.runningOffset -= config.offsetLength;
      }
      // Finished Builds
      else {
        this.options.offset = Math.max(this.options.offset - config.offsetLength, 0);
      }

    }
  
    else if (hasScrolled === 'down') {
      this.options.offset = this.data.nextOffset;

      if ((this.options.offset + config.offsetLength) > this.options.logSize) {
        this.endOfLogLoaded = true;
      }

      // if we've loaded a partial offset
      if (this.options.offset < config.offsetLength  && this.options.offset > 0) {
        this.options.offset = config.offsetLength;
        this.endOfLogLoaded = true;
      }
    }

    return this;
  }
  
  reset() {
    this.fetchCount = 0;
    this.hasFetched = false;
    this.endOfLogLoaded = false;
    this.startOfLogLoaded = false;
    this.logLines = [];
    return this;
  }

  setOffset(offset) {
    this.options.currentOffset = offset;
    this.options.offset = offset;
    return this;
  }

  formatLog() {
    if (this.jqXHR.status !== 200) {
      console.warn(this.jqXHR);
      return "<p class='roomy-xy'>Error loading build log. Please check your console for more detail.</p>";
    }

    let offsetRunningTotal = this.options.offset;

    const NEW_LINE = '\n';
    let logData = this.jqXHR.responseJSON.data;

    // If we've reached the top when scrolling up,
    // we need to omit any overlap from last fetch..
    if (this.options.offset === 0 && this.hasScrolled === 'up') {
      logData = logData.substring(0, getByteLength(logData) - (config.offsetLength - this.previousOffset))
    }

    if (logData.length === 0) {
      return [];
    }

    const splitLines = compact(logData.split(NEW_LINE));
    return splitLines.map((line, i) => {
      // store second line because we may chop off the first
      if (i === 1) {
        this.currentOffsetLine = offsetRunningTotal + getByteLength(line);
      }

      if (i === splitLines.length - 1) {
        this.lastOffsetLine = offsetRunningTotal + getByteLength(length);
      }

      return {
        text:  utf8.decode(line),
        offset: offsetRunningTotal += getByteLength(line)
      }
    });

  }

}

export default Log;
