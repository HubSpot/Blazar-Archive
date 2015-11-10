/*global config*/
import Model from './Model';
import BuildStates from '../constants/BuildStates';
import utf8 from 'utf8';

class Log extends Model {
  
  constructor(options) {
    this.logLines = [];
    this.hasFetched = false;
    super(options);
  }

  url() {
    console.log('fetching: ', this.options.offset);
    return `${config.apiRoot}/build/${this.options.buildNumber}/log?offset=${this.options.offset}&length=50000`;
  }
  
  parseInProgressBuild() {    
    // if (this.nextOffset === this.data.nextOffset) {
    //   console.log('no change in log');
    //   return;
    // }
    

    // initial load
    if (!this.hasFetched) {
      this.logLines = this.formatLog();
      this.hasFetched = true;
    }
    
    // paging up
    else if (this.isPaging && (this.positionChange === 'top' || this.direction === 'up')) {
      const newLogLines = this.formatLog();
      this.logLines = newLogLines.concat(this.logLines);
    }

    else {
      this.logLines = this.logLines.concat(this.formatLog());
    }
    
    // else {
    //   console.log('more to go...');
    //   this.logLines = this.logLines.concat(this.formatLog());
    //   this.setOffset()
    //   
    // }

  }
  
  parseInactiveBuild() {
    // paging up
    if (this.isPaging && (this.positionChange === 'top' || this.direction === 'up')  ) {
      const newLogLines = this.formatLog();
      this.logLines = newLogLines.concat(this.logLines);
    }
    // initial offset load or paging down
    else {
      this.logLines = this.logLines.concat(this.formatLog());
    }

  }
  
  parse() {
    if (this.options.buildState === BuildStates.IN_PROGRESS) {
      this.parseInProgressBuild();
    }
    else {
      this.parseInactiveBuild();
    }

    // keep track of if we've loaded the last page
    if (this.options.offset === this.options.lastOffset) {
      this.endOfLogLoaded = true;
    }

  }

  incrementPage() {
    this.currentPage += 1;
  }

  decrementPage() {
    this.currentPage -= 1;
  }

  pageLog(direction) {
    console.log('pageLog');
    this.isPaging = true;
    this.previousOffset = this.options.offset;

    if (direction === 'up') {
      
      // if in progress build
      if (this.options.buildState === BuildStates.IN_PROGRESS) {
        this.options.offset = Math.max(this.options.offset - config.offsetLength, 0);        
        // this.options.offset = Math.max(this.options.startingOffset - config.offsetLength, 0);
        console.log('set offset: ', this.options.offset);
      }
      // finished build
      else {
        this.options.offset = Math.max(this.options.offset - config.offsetLength, 0);        
      }

    }

    else {
      this.options.offset = Math.min(this.options.offset + config.offsetLength, this.options.lastOffset);
      // if we've loaded a partial offset
      if (this.options.offset < config.offsetLength && this.options.offset > 0) {        
        this.options.offset = config.offsetLength;
        this.endOfLogLoaded = true;
      }
    }

    return this;
  }
  
  reset() {
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

    // If offset is less than our offsetLength when paging up,
    // we need to omit any overlap from last fetch
    if (this.previousOffset < config.offsetLength && this.direction === 'up' && !this.hasNavigatedWithButtons) {
      logData = logData.substring(0, this.getByteLength(logData) - (config.offsetLength - this.previousOffset))
    }

    if (logData.length === 0) {
      return [];
    }

    const splitLines = logData.split(NEW_LINE);
    
    return splitLines.map((line, i) => {
      if (i === 0) {
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
  // omit last line that may be incomplete
  // }).splice(0, splitLines.length - 1);
    
  }

}

export default Log;
