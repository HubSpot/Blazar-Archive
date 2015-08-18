// __tests__/navigation-test.js

jest.dontMock('../components/navigation/navigation.jsx');
jest.dontMock('../../../test-utils/stubRouterContext.jsx');

var React = require('react/addons');
var Navigation = require('../components/navigation/navigation.jsx');

var StubRouterContext = require('../../../test-utils/stubRouterContext.jsx');

var TestUtils = React.addons.TestUtils;

describe('testing a component', function() {

  it('shows that the logo is Blazar', function() {

    var NavigationWithRouterContext = StubRouterContext(Navigation),
      navigation = TestUtils.renderIntoDocument(<NavigationWithRouterContext />);
      logo = TestUtils.findRenderedDOMComponentWithClass(navigation, 'navbar-brand'),

    expect(React.findDOMNode(logo).textContent).toEqual('Blazar');
  });

});