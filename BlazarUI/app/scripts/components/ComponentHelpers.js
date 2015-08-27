import moment from 'moment';

let ComponentHelpers = {

  // 'DRIVER_NOT_RUNNING' => 'Driver not running'
  humanizeText: function(string){
    if (!string){
      return '';
    }
    string = string.replace(/_/g, ' ');
    string = string.toLowerCase();
    string = string[0].toUpperCase() + string.substr(1)
    return string
  },

  // 12345 => 12 seconds
  timestampDuration: function (timestamp){
    if (!timestamp){
      return '';
    }
    return moment.duration(timestamp).humanize();
  },

  truncate: function (str, len) {
    if (str && str.length > len && str.length > 0) {
      var new_str = str + " ";
      new_str = str.substr (0, len);
      new_str = str.substr (0, new_str.lastIndexOf(" "));
      new_str = (new_str.length > 0) ? new_str : str.substr (0, len);
      return new_str;
    }
    return str;
  },

  // 1234567890 => 1 Aug 1991 15:00
  timestampFormatted: function (timestamp){
    if(!timestamp) {
      return '';
    }
    let timeObject = moment(timestamp);
    return timeObject.format('lll');
  },

  githubShaLink: function(info) {
    return `https://github.com/${info.gitInfo.organization}/${info.gitInfo.repository}/commit/${info.build.sha}/`;
  }

}

export default ComponentHelpers;
