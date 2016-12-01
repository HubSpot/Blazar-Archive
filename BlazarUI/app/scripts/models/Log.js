import Model from './Model';
import BuildStates from '../constants/BuildStates';
import utf8 from 'utf8';
import {getByteLength} from '../utils/logHelpers';
import {rest, initial, first, last, compact} from 'underscore';

class Log extends Model {

  constructor(options) {
    this.init(options);
    super(options);
  }

  init(options) {
    this.logLines = [];
    this.fetchCount = 0;
    this.baseRequestLength = window.config.offsetLength;
    this.requestOffset = this.getMaxOffset(options.size);
    this.lengthOverride = false;
    this.shouldPoll = options.buildState === BuildStates.IN_PROGRESS;
    this.currentOffsetLine = null;
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
    if (this.requestOffset === -1) {
      console.warn('requestOffset not properly set');
      return null;
    }
    return `${window.config.apiRoot}/modules/builds/${this.options.buildId}/log?offset=${this.requestOffset}&length=${this.lengthOverride || this.baseRequestLength}`;
  }

  getMaxOffset(size) {
    return Math.max(size - this.baseRequestLength, 0);
  }

  parseInactiveBuild() {
    // First fetch or navigated 'To Bottom'
    if (!this.fetchAction || this.fetchAction === 'bottom') {
      this.handleEndOfLogFetch();
    } else if (this.fetchAction === 'top') {
      // Navigated 'To Top'
      this.handleToTopFetch();
    } else if (this.fetchAction === 'next') {
      // Scrolling Down
      this.handleNextFetch();
    } else if (this.fetchAction === 'previous') {
      // Scrolling up
      this.handlePreviousFetch();
    } else {
      console.warn('Parse condition not met. ', this);
    }
  }

  parseActiveBuild() {
    // Scrolling up
    if (this.fetchAction === 'previous') {
      this.handlePreviousFetch();
    } else if (this.fetchCount === 1) {
      this.handleFirstActiveFetch();
    } else {
      this.handleMoreActiveFetch();
    }
  }

  parse() {
    this.data = this.raw;
    this.fetchCount++;
    this.fetchTimestamp = Date.now();
    this.maxOffsetLoaded = Math.max(this.data.nextOffset, this.maxOffsetLoaded);
    this.minOffsetLoaded = Math.min(this.requestOffset, this.minOffsetLoaded);
    this.buildInProgress = this.options.buildState === BuildStates.IN_PROGRESS;
    this.cancelledBuild = this.options.buildState === BuildStates.CANCELLED;
    this.endOfLogLoaded = this.maxOffsetLoaded === this.options.size;
    this.newLogLines = this.formatLog();

    if (this.buildInProgress) {
      this.parseActiveBuild();
    } else {
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
    } else if (this.options.size > this.baseRequestLength) {
      // our log is larger than at least 1 offset
      // e.g. we used the "To Top" button
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
    } else if (!this.shouldPoll) {
      // if we are not polling
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
  parseNextFetch() {
    // save incomplete last line so we can prepend it to the
    // incomplete first line of the next fetch if we continue to scroll down
    const tempLast = last(this.newLogLines);
    // chop off last incomplete line, as long as we have more to fetch
    if (!this.endOfLogLoaded) {
      this.newLogLines = initial(this.newLogLines);
    }
    // prepend the rest of the first line that was cutoff last fetch
    if (this.lastLine && this.newLogLines.length) {
      this.newLogLines[0].text = this.lastLine.text + this.newLogLines[0].text;
    }
    this.lastLine = tempLast;
    this.logLines = [...this.logLines, ...this.newLogLines];
  }

  // Helper for parse, used to manage incomplete lines when scrolling down
  parsePreviousFetch(options = {}) {
    const {removeFirstLine} = options;
    // save incomplete first line so we can append it to the
    // incomplete last line of the next fetch if we continue to scroll up
    const tempFirst = first(this.newLogLines);
    // remove the first line which may be incomplete
    // as long as we are not at the beginning of the log
    if (this.requestOffset !== 0 || removeFirstLine) {
      this.newLogLines = rest(this.newLogLines);
    }

    if (this.firstLine && this.newLogLines.length) {
      // append extra text to last log line that is incomplete
      const lastIndex = this.newLogLines.length - 1;
      this.newLogLines[lastIndex].text += this.firstLine.text;
    }
    this.firstLine = tempFirst;

    // prepend new logLines to existing logLines
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

    if (!logData || logData.match(WHITE_SPACE)) {
      return [];
    }

    let splitLines = logData.split(NEW_LINE);

    // Split results in an extra "" element if
    // log data ends with a new line. Do not count
    // this as an additional parsed line.
    if (logData.endsWith(NEW_LINE)) {
      splitLines = initial(splitLines);
    }

    let offsetRunningTotal = this.requestOffset;

    const formattedLogLines = splitLines.map((line, i) => {
      const currentOffset = offsetRunningTotal;

      // store second line because we may chop off the first
      if (i === 1) {
        this.currentOffsetLine = currentOffset;
      }

      try {
        line = utf8.decode(line);
      } catch (err) {
        console.warn("We couldn't decode some UTF-8, so we're displaying this log line undecoded:\n", line);
      }

      // increment for next loop interation
      offsetRunningTotal += getByteLength(line + NEW_LINE);

      return {
        text: line,
        offset: currentOffset
      };
    });

    return compact(formattedLogLines);
  }


}

export default Log;
