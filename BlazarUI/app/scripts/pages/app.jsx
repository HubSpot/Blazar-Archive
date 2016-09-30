import React, {PropTypes} from 'react';
import DocumentTitle from 'react-document-title';
import SidebarContainer from '../components/sidebar/SidebarContainer.jsx';
import FeedbackForm from '../components/feedback/FeedbackForm.jsx';

const App = ({params, children}) => {
  return (
    <DocumentTitle title="Blazar">
      <div>
        <div className="page-wrapper">
          <SidebarContainer params={params} />
          {children}
        </div>
        <FeedbackForm />
      </div>
    </DocumentTitle>
  );
};

App.propTypes = {
  params: PropTypes.object.isRequired,
  children: PropTypes.node.isRequired
};

export default App;
