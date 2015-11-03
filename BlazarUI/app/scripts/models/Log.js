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
    if (this.isPaging) {
      const newLogLines = this.formatLog();
      this.logLines = newLogLines.concat(this.logLines);
    }

    else {
      this.logLines = this.logLines.concat(this.formatLog());
    }
  }
  
  pageUp() {
    console.log('pageUp time. Current offset: ', this.options.offset);
    this.isPaging = true;
    this.options.offset = Math.max(this.options.offset - config.offsetLength, 0);
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
    // omit last line
    // omit the first (incomplete) element unless we're at the beginning of the file
    const splitLines = this.jqXHR.responseJSON.data.split(NEW_LINE);

    return splitLines.map((line, i) => {
      if (i === 0) {
        this.currrentOffsetLine = offsetRunningTotal + line.length;
      }
      
      if (i === splitLines.length - 1) {
        this.lastOffsetLine = offsetRunningTotal + line.length;
      }
      // To Do
      // use byte size not character length
      return {
        text: line,
        offset: offsetRunningTotal += line.length
      }
      
    });

  }

}

export default Log;
