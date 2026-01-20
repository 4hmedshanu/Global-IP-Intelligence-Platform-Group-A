import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { GoogleOAuthProvider } from '@react-oauth/google';
import { ThemeProvider } from "./context/ThemeContext";
import LandingPage from "./components/LandingPage";
import Login from "./components/Login";
import Register from "./components/Register";
import OAuth2RedirectHandler from "./components/OAuth2RedirectHandler";
import ForgotPassword from "./components/ForgotPassword";
import ResetPassword from "./components/ResetPassword";
import AdminDashboard from "./components/AdminDashboard";
import UserDashboard from "./components/UserDashboard";
import AnalystDashboard from "./components/AnalystDashboard";
import SearchPage from "./components/SearchPage";
import IpAssetDetailPage from "./components/IpAssetDetailPage";
import IpAssetQuickView from "./components/IpAssetQuickView";
import ProtectedRoute from "./components/ProtectedRoute";

const GOOGLE_CLIENT_ID = process.env.REACT_APP_GOOGLE_CLIENT_ID || "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com";

function App() {
  return (
    <ThemeProvider>
      <GoogleOAuthProvider clientId={GOOGLE_CLIENT_ID}>
        <Router>
          <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
            <Route path="/oauth2/success" element={<OAuth2RedirectHandler />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />
            
            {/* Protected Routes - Only accessible with authentication and correct role */}
            <Route 
              path="/admindashboard" 
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminDashboard />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/userdashboard" 
              element={
                <ProtectedRoute allowedRoles={['USER']}>
                  <UserDashboard />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/analystdashboard" 
              element={
                <ProtectedRoute allowedRoles={['ANALYST']}>
                  <AnalystDashboard />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/search" 
              element={
                <ProtectedRoute allowedRoles={['USER', 'ANALYST', 'ADMIN']}>
                  <SearchPage />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/ip-asset/:id" 
              element={
                <ProtectedRoute allowedRoles={['USER', 'ANALYST', 'ADMIN']}>
                  <IpAssetDetailPage />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/ip-asset-quick-view" 
              element={
                <ProtectedRoute allowedRoles={['USER', 'ANALYST', 'ADMIN']}>
                  <IpAssetQuickView />
                </ProtectedRoute>
              } 
            />
            
            {/* Redirect any unknown route to landing page */}
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </Router>
      </GoogleOAuthProvider>
    </ThemeProvider>
  );
}

export default App;
