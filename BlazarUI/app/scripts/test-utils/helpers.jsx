import React from 'react/addons';
import { extend } from 'underscore';
const TestUtils = React.addons.TestUtils;

function simulate (eventType, node) {
  TestUtils.Simulate[eventType](node);
}

module.exports = {
  render: function(Component, options) {
    extend({}, options);
    return TestUtils.renderIntoDocument(Component(options));
  },
  click: function (node) {
    simulate('click', node);
  }
};

export const renderedOutput = function(elt) {
  const shallowRenderer = TestUtils.createRenderer();
  shallowRenderer.render(elt);
  return shallowRenderer.getRenderOutput();
}
