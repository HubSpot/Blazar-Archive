import { combineReducers } from 'redux';
import branchState from './branchState';

import tailer from 'singularityui-tailer/src/reducers';

export default combineReducers({
  branchState,
  tailer
});
