jest.dontMock('../../components/shared/Icon.jsx');
jest.dontMock('classnames');

import React from 'react/addons';
import Icon from '../../components/shared/Icon.jsx';

const TestUtils = React.addons.TestUtils;

function renderedOutput(elt) {
  const shallowRenderer = TestUtils.createRenderer();
  shallowRenderer.render(elt);
  return shallowRenderer.getRenderOutput();
}

describe('<Icon />', () => {

  it('should generate correct classes for octicons', () => {
    const result = renderedOutput(<Icon type='octicon' name='file-directory' classNames="icon-roomy" />);
    expect(result.props.className).toEqual('icon icon-roomy octicon octicon-file-directory');
  });

  it('should generate correct classes for font-awesome icons', () => {
    const result = renderedOutput(<Icon name='arrow-circle-up' />);
    expect(result.props.className).toEqual('icon fa fa-arrow-circle-up');
  });

  it('should generate the correct title', () => {
    const result = renderedOutput(<Icon type='octicon' title='The arsonist has oddly shaped feet' name='file-directory' classNames="icon-roomy" />);
    expect(result.props.title).toEqual('The arsonist has oddly shaped feet');
  });

});
