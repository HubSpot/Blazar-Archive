import React from 'react';
import { extend } from 'underscore';
const TestUtils = React.addons.TestUtils;

function simulate (eventType, node) {
  TestUtils.Simulate[eventType](node);
}

module.exports = {
  render: (Component, options) => {
    extend({}, options);
    return TestUtils.renderIntoDocument(Component(options));
  },
  click: (node) => {
    simulate('click', node);
  }
};

export const renderedOutput = (elt) => {
  const shallowRenderer = TestUtils.createRenderer();
  shallowRenderer.render(elt);
  return shallowRenderer.getRenderOutput();
};
