jest.dontMock('../../components/shared/TableHead.jsx');

import React from 'react/addons';
import TableHead from '../../components/shared/TableHead.jsx';

const TestUtils = React.addons.TestUtils;

function renderedOutput(elt) {
  const shallowRenderer = TestUtils.createRenderer();
  shallowRenderer.render(elt);
  return shallowRenderer.getRenderOutput();
}

describe('<TableHead />', () => {

  let tableHead;
  let mockColumns = [
    {label: 'Module', key: 'module'},
    {label: 'Latest Build', key: 'latestBuild'},
    {label: 'Start Time', key: 'startTime'}
  ];

  beforeEach(() => {
    tableHead = renderedOutput(<TableHead columnNames={mockColumns} />);
  });

  it('should generate a th', () => {
    expect(tableHead.type).toBe('thead');
  });

  it('should generate children with table rows', () => {
    expect(tableHead.props.children.type).toBe('tr');
  });

});
