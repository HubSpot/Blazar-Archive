jest.dontMock('../../components/shared/TableHeadRow.jsx');

import React from 'react/addons';
import TableHeadRow from '../../components/shared/TableHeadRow.jsx';

const TestUtils = React.addons.TestUtils;

function renderedOutput(elt) {
  const shallowRenderer = TestUtils.createRenderer();
  shallowRenderer.render(elt);
  return shallowRenderer.getRenderOutput();
}

describe('<TableHeadRow />', () => {

  let tableHeadRow;

  beforeEach(() => {
    tableHeadRow = renderedOutput(<TableHeadRow label='A tarantula enjoys a fine chewing gum' />);
  });

  it('should generate a th', () => {
    expect(tableHeadRow.type).toBe('th');
  });

  it('should generate a label', () => {
    expect(tableHeadRow.props.children).toEqual('A tarantula enjoys a fine chewing gum');
  });

});
