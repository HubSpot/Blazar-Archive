/*global config*/
import Model from './Model';
import BuildStates from '../constants/BuildStates';
import utf8 from 'utf8';
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

    // If build is In Progress and we are not starting at the beginning
    // we need to get rid of first line and keep track of it if user scrolls up
    if (this.fetchCount === 1 && buildInProgress) {
      this.lastLine = last(newLogLines); // .........
      this.firstLine = first(newLogLines);
      
      // newLogLines = rest(newLogLines);
      
      if (this.options.offset > 0) {
        this.firstLine = first(newLogLines);
        newLogLines = rest(newLogLines)    
      }
      
    }



    // Scrolling up, but not Navigating up
    if (this.isPaging && this.direction === 'up' && this.positionChange !== 'top') {
      // save incomplete first line so we can append it to the 
      // incomplete last line of the next fetch  if we scroll up
      const tempFirst = first(newLogLines);
      // remove the first line which may be incomplete
      newLogLines = rest(newLogLines);

      // append any extra text to last log line
      newLogLines[newLogLines.length - 1].text = newLogLines[newLogLines.length - 1].text + this.firstLine.text;
      
      this.logLines = newLogLines.concat(this.logLines);
      this.firstLine = tempFirst;
    }


    // Initial load with finished builds, used nav buttons, or scrolling down
    else {
      // If we started with a log size less than one offset we dont need to fix any lines.
      // e.g. we have been tailing the log from the very start
      if (this.options.logSize < config.offsetLength) {}

      // Button Navigated to the end of the log
      else if (this.options.startingOffset === this.options.offset && (!buildInProgress || this.fetchCount !== 1) ) {
        // save the first incomplete line so we can append it to 
        // the last incomplete line on the next fetch if we scroll up        
        this.firstLine = first(newLogLines);
        // remove the first incomplete line
        newLogLines = rest(newLogLines);
      }
      
      // Button Navigated to the top of a finished build
      // or scrolling down
      else {
        // save incomplete last line so we can prepend it to 
        // the incomplete first line of next fetch if we scroll down
        const tempLast = last(newLogLines); 

        if (!buildInProgress) {
          newLogLines = initial(newLogLines);
        }

        // append any extra text to first log line            
        if (newLogLines[0] && this.lastLine) {
          // if we are loading less than 1 offset at a time
          if (buildInProgress && this.nextOffset !== config.offsetLength) {
            if (!this.isPolling) {
              newLogLines = initial(newLogLines)
            }
          }
          // if we have a full offset to load
          else {            
            if (!this.isPolling) {
              newLogLines = initial(newLogLines);
            }

            newLogLines[0].text = this.lastLine.text + newLogLines[0].text;  
          }
        }

        this.lastLine = tempLast;
      }

      this.logLines = this.logLines.concat(newLogLines);
    }
  }

  pageLog(direction) {
    this.isPaging = true;
    this.previousOffset = this.options.offset;

    if (direction === 'up') {
      // Builds In Progresse
      if (this.options.buildState === BuildStates.IN_PROGRESS) {
        this.options.offset = Math.max(this.runningOffset - config.offsetLength - 1, 0);
        this.runningOffset -= config.offsetLength;
      }
      // Finished Builds
      else {
        this.options.offset = Math.max(this.options.offset - config.offsetLength - 1, 0);
      }

    }
  
    else if (direction === 'down') {
      
      if (this.options.offset === 0) {
        // this.options.offset = config.offsetLength + 1;
        this.options.offset = config.offsetLength ;
      }
      else {
        // this.options.offset = this.options.offset + config.offsetLength + 1;
        this.options.offset = this.options.offset + config.offsetLength;
        
        if ((this.options.offset + config.offsetLength + 1) > this.options.logSize) {
          this.endOfLogLoaded = true;
        }

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
    this.logLines = [];
    return this;
  }

  setOffset(offset) {
    this.options.currentOffset = offset;
    this.options.offset = offset;
    return this;
  }

  getByteLength(normal_val) {
      // Force string type
      normal_val = String(normal_val);

      let byteLen = 0;
      for (let i = 0; i < normal_val.length; i++) {
          const c = normal_val.charCodeAt(i);
          byteLen += c < (1 <<  7) ? 1 :
                     c < (1 << 11) ? 2 :
                     c < (1 << 16) ? 3 :
                     c < (1 << 21) ? 4 :
                     c < (1 << 26) ? 5 :
                     c < (1 << 31) ? 6 : Number.NaN;
      }
      return byteLen;
  }

  formatLog() {
    if (this.jqXHR.status !== 200) {
      console.warn(this.jqXHR);
      return "<p class='roomy-xy'>Error loading build log. Please check your console for more detail.</p>";
    }

    let offsetRunningTotal = this.options.offset;

    const NEW_LINE = '\n';
    let logData = this.jqXHR.responseJSON.data;

    // If offset is less than our offsetLength when scrolling up,
    // and we havent used the 'To Top' navigation button
    // we need to omit any overlap from last fetch..    
    if (this.options.offset < config.offsetLength && this.direction === 'up') {
      logData = logData.substring(0, this.getByteLength(logData) - (config.offsetLength + 1 - this.previousOffset))
    }

    if (logData.length === 0) {
      return [];
    }

    const splitLines = compact(logData.split(NEW_LINE));
    return splitLines.map((line, i) => {
      // store second line because we may chop off the first
      if (i === 1) {
        this.currentOffsetLine = offsetRunningTotal + this.getByteLength(line);
      }

      if (i === splitLines.length - 1) {
        this.lastOffsetLine = offsetRunningTotal + this.getByteLength(length);
      }

      return {
        text:  utf8.decode(line),
        offset: offsetRunningTotal += this.getByteLength(line)
      }
    });

  }

}

export default Log;
