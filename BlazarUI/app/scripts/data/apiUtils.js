export const getErrorMessage = (jqXHR) => {
  return (jqXHR.responseJSON && jqXHR.responseJSON.message) ||
    jqXHR.responseText || `Status ${jqXHR.status} - ${jqXHR.statusText}`;
};
