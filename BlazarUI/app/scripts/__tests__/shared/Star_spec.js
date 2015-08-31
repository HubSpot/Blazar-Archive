jest.dontMock('../../components/shared/Star.jsx');

import React from 'react/addons';
import Star from '../../components/shared/Star.jsx';

const TestUtils = React.addons.TestUtils;

function renderedOutput(elt) {
  const shallowRenderer = TestUtils.createRenderer();
  shallowRenderer.render(elt);
  return shallowRenderer.getRenderOutput();
}

describe('<Star />', () => {

  let shallowStar, star, button;

  beforeEach(() => {
    shallowStar = renderedOutput(<Star repo='Blazar' branch='master'></Star>);
    star = TestUtils.renderIntoDocument(<Star repo='apple' branch='bacon'></Star>);
    button = TestUtils.findRenderedDOMComponentWithClass(
      star, 'sidebar__star'
    );
  });

  it('should have the right tag', () => {
    expect(shallowStar.type).toBe('span');
  });

  it('should call event handler when clicked', () => {
    star.setState({ starred: false });
    expect(button.props.className).toBe('sidebar__star unselected');
  });


});
