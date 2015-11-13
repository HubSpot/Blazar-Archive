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
  
    if (this.options.offset === this.options.lastOffset) {
      this.endOfLogLoaded = true;
    }
    
    if (this.options.offset === 0) {
      this.startOfLogLoaded = true;
    }

    // nothing new, nothing to do
    if (newLogLines.length === 0) {
      this.logLines = this.logLines;
      return;
    }

    // scrolling up, but not navigating up
    if (this.isPaging && this.direction === 'up' && this.positionChange !== 'top') {
      
      // save first line so we can 
      // append it during the next fetch
      const tempFirst = first(newLogLines);

      this.lastLine = last(newLogLines);
      newLogLines = rest(newLogLines);

      // append any extra text to last log line
      newLogLines[newLogLines.length - 1].text = newLogLines[newLogLines.length - 1].text + this.firstLine.text;
      
      this.logLines = newLogLines.concat(this.logLines);
      this.firstLine = tempFirst;
    }

    // initial offset load, used nav buttons, or paging down
    else {
      // If we only have one page, nothing to do here
      if (this.options.offset === 0 && this.options.logSize < config.offsetLength) {}
      // bottom of page
      else if (this.options.startingOffset === this.options.offset) {
        this.lastLine = last(newLogLines);
        this.firstLine = first(newLogLines);
        newLogLines = rest(newLogLines);
      }
      // top of page
      else if (this.options.offset === 0) {
        this.lastLine = last(newLogLines);
        newLogLines = initial(newLogLines);
      }
      // in between top and bottom
      else {
        const tempLast = last(newLogLines);
        newLogLines = initial(newLogLines);
        
        // append any extra text to first log line            
        if (newLogLines[0] && this.lastLine) {
          newLogLines[0].text = this.lastLine.text + newLogLines[0].text;
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
        this.options.offset = config.offsetLength + 1;
      }
      else {
        this.options.offset = this.options.offset + config.offsetLength + 1;
        
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
        this.currrentOffsetLine = offsetRunningTotal + this.getByteLength(line);
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
