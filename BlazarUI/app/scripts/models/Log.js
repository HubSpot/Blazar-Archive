/*global config*/
import Model from './Model';
// import utf8 from 'utf8';

class Log extends Model {
  
  constructor(options) {
    this.logLines = [];
    super(options);
  }

  url() {
    return `${config.apiRoot}/build/${this.options.buildNumber}/log?offset=${this.options.offset}`;
  }
  
  parse() {
    // keep track of if we've loaded the last page
    if (this.options.offset === this.options.lastOffset) {
      this.endOfLogLoaded = true;
    }

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

  incrementPage() {
    this.currentPage += 1;
  }

  decrementPage() {
    this.currentPage -= 1;
  }

  pageLog(direction) {
    this.isPaging = true;
    this.previousOffset = this.options.offset;

    if (direction === 'up') {
      this.options.offset = Math.max(this.options.offset - config.offsetLength, 0);
    }

    else {
      this.options.offset = Math.min(this.options.offset + 90000, this.options.lastOffset);
      // if we've loaded a partial offset
      if (this.options.offset < 90000 && this.options.offset > 0) {
        this.options.offset = 90000;
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

  formatLog() {
    if (this.jqXHR.status !== 200) {
      console.warn(this.jqXHR);
      return "<p class='roomy-xy'>Error loading build log. Please check your console for more detail.</p>";
    }

    let offsetRunningTotal = this.options.offset;
    const NEW_LINE = '\n';
  
    // To Do:
    // use bytes not character length
    // omit last line
    // omit the first (incomplete) element unless we're at the beginning of the file
    let logData = this.jqXHR.responseJSON.data;

    // If offset is less that 90000 when paging up, 
    // we need to remove any duplicate lines
    // TO DO: trim is not cleanly cutting the line    
    if (this.previousOffset < 90000 && this.direction === 'up' && !this.hasNavigatedWithButtons) {
      logData = logData.substring(0, logData.length - (90000 - this.previousOffset))
    }
    
    const splitLines = logData.split(NEW_LINE);

    return splitLines.map((line, i) => {
      if (i === 0) {
        this.currrentOffsetLine = offsetRunningTotal + line.length;
      }
      
      if (i === splitLines.length - 1) {
        this.lastOffsetLine = offsetRunningTotal + line.length;
      }

      return {
        text: line,
        offset: offsetRunningTotal += line.length
      }
      
    });

  }

}

export default Log;
