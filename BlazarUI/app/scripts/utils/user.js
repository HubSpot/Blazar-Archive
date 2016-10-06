import Cookies from 'js-cookie';

const { betaUserCookie } = window.config;

export const isBetaUser = betaUserCookie && !!Cookies.getJSON(betaUserCookie);
