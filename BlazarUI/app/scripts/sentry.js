import { getUsernameFromCookie } from './components/Helpers.js';
import $ from 'jquery';

const username = getUsernameFromCookie();

const captureAjaxError = (event, xhr, ajaxSettings) => {
  const {status, statusText, responseText, responseJSON} = xhr;
  const {type, url} = ajaxSettings;

  window.Raven.captureMessage('Ajax Error', {
    extra: {
      status,
      statusText,
      responseText,
      responseJSON,
      requestMethod: type,
      requestUrl: url,
      url: window.location.href
    }
  });
};

export const configureSentry = () => {
  window.Raven.setUserContext({username});
  $(document).ajaxError(captureAjaxError);
};
