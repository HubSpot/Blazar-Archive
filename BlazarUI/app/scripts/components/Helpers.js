// Using in favor of Deprecated ComponentHelpers.js

import moment from 'moment';

// 1234567890 => 1 Aug 1991 15:00
export const timestampFormatted = function(timestamp, format='lll') {
  timestamp = parseInt(timestamp);
  if (!timestamp) {
    return '';
  }
  const timeObject = moment(timestamp);
  return timeObject.format(format);
};

// 'BUILD_SUCCEEEDED' => 'Build Succeeded'
export const humanizeText = function(string) {
  if (!string) {
    return '';
  }
  string = string.replace(/_/g, ' ');
  string = string.toLowerCase();
  string = string[0].toUpperCase() + string.substr(1);
  return string;
};


export const truncate = function(str, len = 10) {
  if (str && str.length > len && str.length > 0) {
    let new_str = str + ' ';
    new_str = str.substr(0, len);
    new_str = str.substr(0, new_str.lastIndexOf(' '));
    new_str = (new_str.length > 0) ? new_str : str.substr(0, len);
    return new_str;
  }
  return str;
};

export const githubShaLink = function(info) {
  return `https://github.com/${info.gitInfo.organization}/${info.gitInfo.repository}/commit/${info.build.sha}/`;
};
