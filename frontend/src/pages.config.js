import Layout from './Layout.jsx';
import Home from './pages/Home.jsx';
import Explore from './pages/Explore.jsx';
import ContentDetail from './pages/ContentDetail.jsx';
import Credits from './pages/Credits.jsx';
import Upload from './pages/Upload.jsx';
import Library from './pages/Library.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Profile from './pages/Profile.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
export const PAGES = {
  Home,
  Explore,
  'content/:slug': ContentDetail,
  Credits,
  Upload,
  Library,
  Dashboard,
  Profile,
  Login,
  Register,
};
export const ADMINS = {};
export const PRIVATE_PAGES = {};
export const pagesConfig = {
  privatePages: PRIVATE_PAGES,
  mainPage: 'Home',
  Pages: PAGES,
  Layout: Layout,
  Admins: ADMINS,
  adminMainPage: '',
  AdminLayout: null,
};