let ComponentHelpers = {

  bindAll: function(scope, arr){
    arr.forEach(function(method){
      scope[method] = scope[method].bind(scope);
    })
  }

}

export default ComponentHelpers;