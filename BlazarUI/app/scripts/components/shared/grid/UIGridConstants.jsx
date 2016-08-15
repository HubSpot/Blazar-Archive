import _ from 'underscore';

const range = _.range;

const MAX_OFFSET = 12;
const MAX_SIZE = 12;
const MIN_OFFSET = 0;
const MIN_SIZE = 1;

const UIGridConstants = {
  MAX_OFFSET,
  MAX_SIZE,
  MIN_OFFSET,
  MIN_SIZE,
  OFFSET_RANGE: range(MIN_OFFSET, MAX_OFFSET + 1),
  SIZE_RANGE: range(MIN_SIZE, MAX_SIZE + 1)
};

export default UIGridConstants;
