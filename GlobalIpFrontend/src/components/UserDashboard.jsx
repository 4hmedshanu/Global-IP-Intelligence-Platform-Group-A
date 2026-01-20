import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Bell, BookmarkIcon, FileText, BarChart3, Activity, TrendingUp, Lightbulb, X, ChevronLeft, ChevronRight, User, Eye } from 'lucide-react';
import { VscSearch, VscSave, VscBell } from 'react-icons/vsc';
import { LineChart, Line, BarChart, Bar, PieChart, Pie, Cell, AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { useTheme } from '../context/ThemeContext';
import DarkModeToggle from './DarkModeToggle';
import Toast from './Toast';
import api from '../api/axios';



// ✅ Detail component (HERE)
const Detail = ({ label, value }) => (
  <div>
    <p className="text-xs text-gray-500">{label}</p>
    <p className="text-sm font-medium">{value || '-'}</p>
  </div>
);

export default function UserDashboard() {
  const navigate = useNavigate();
  const { theme } = useTheme();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');
  const [searchQuery, setSearchQuery] = useState('');
  const [searchType, setSearchType] = useState('patents');
  const [searchFilters, setSearchFilters] = useState({
    jurisdiction: '',
    technology: '',
    dateFrom: '',
    dateTo: ''
  });
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  const [notifications, setNotifications] = useState([
    { id: 1, title: 'New Patent Filed', message: 'AI/ML patent in US jurisdiction', time: '2h ago', read: false },
    { id: 2, title: 'Subscription Expiring', message: 'Renew your subscription', time: '1d ago', read: false },
    { id: 3, title: 'Search Alert', message: '5 new results for your saved search', time: '3d ago', read: true }
  ]);

  const [searchResults, setSearchResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchHistory, setSearchHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [showProfileEdit, setShowProfileEdit] = useState(false);
  const [showProfileView, setShowProfileView] = useState(false);
  const [editForm, setEditForm] = useState({
    firstName: '',
    lastName: '',
    email: ''
  });
  const [toast, setToast] = useState({ show: false, message: '', type: 'success' });
  // View Asset Modal
  const [showAssetModal, setShowAssetModal] = useState(false);
  const [selectedAsset, setSelectedAsset] = useState(null);


  // ================= Milestone 3: Subscriptions =================
  const [subscribedIds, setSubscribedIds] = useState([]);

  // 🔔 Subscriptions Feature (Separate)
  const [subscriptions, setSubscriptions] = useState([]);
  const [subsLoading, setSubsLoading] = useState(false);



  const [dashboardStats, setDashboardStats] = useState({
    totalSearches: 0,
    savedItems: 0,
    activeAlerts: 0,
    reports: 0,
    activityData: [],
    technologyData: []
  });

  // Fetch dashboard data whenever overview tab is activated
  useEffect(() => {

    const fetchDashboard = async () => {
      try {
        const res = await api.get('/api/dashboard/user');
        setDashboardStats(res.data);
      } catch (err) {
        console.error('Error fetching dashboard data:', err);
      }
    };

    if (activeTab === 'overview') {
      fetchDashboard();
    }

    if (activeTab === 'search') {
      fetchSearchHistory();
    }

    if (activeTab === 'monitoring') {
      fetchFilingTracker();
    }

    if (activeTab === 'subscriptions') {
      fetchSubscriptions();
    }



  }, [activeTab]);




  // ================= Milestone 3 =================

  // Filing Tracker
  const [trackedAssets, setTrackedAssets] = useState([]);
  const [trackerLoading, setTrackerLoading] = useState(false);

  // Legal Status Summary
  const [statusStats, setStatusStats] = useState({
    application: 0,
    granted: 0,
    expired: 0,
    renewal: 0
  });



  useEffect(() => {
    const fetchProfile = async () => {
      const token = localStorage.getItem('token');
      const userData = localStorage.getItem('user');

      if (!token || !userData) {
        setLoading(false);
        navigate('/login');
        return;
      }

      // Load from localStorage immediately
      const parsedUser = JSON.parse(userData);
      setUser(parsedUser);
      setEditForm({
        firstName: parsedUser.firstName || '',
        lastName: parsedUser.lastName || '',
        email: parsedUser.email || ''
      });
      setLoading(false);

      // Then try to fetch fresh data in background
      try {
        const response = await api.get('/profile');
        setUser(response.data);
        setEditForm({
          firstName: response.data.firstName || '',
          lastName: response.data.lastName || '',
          email: response.data.email || ''
        });
        localStorage.setItem('user', JSON.stringify(response.data));
      } catch (error) {
        console.error('Error fetching profile (using cached data):', error);
        // Keep using localStorage data, don't redirect
      }
    };

    fetchProfile();
    if (activeTab === 'search') {
      fetchSearchHistory();
    }
  }, [navigate, activeTab]);

  const fetchSearchHistory = async () => {
    setHistoryLoading(true);
    try {
      const response = await api.get('/api/search/history?page=0&size=10');
      setSearchHistory(response.data);
    } catch (error) {
      console.error('Error fetching search history:', error);
    } finally {
      setHistoryLoading(false);
    }
  };

  // ================= Milestone 3 =================
  // Fetch Filing Tracker (User)
  const fetchFilingTracker = async () => {
    setTrackerLoading(true);
    try {
      const res = await api.get('/api/tracker/my');
      console.log('TRACKER RESPONSE:', res.data);

      setTrackedAssets(res.data.assets || []);

      setStatusStats({
        application: res.data.statusSummary?.application || 0,
        granted: res.data.statusSummary?.granted || 0,
        expired: res.data.statusSummary?.expired || 0,
        renewal: res.data.statusSummary?.renewal || 0
      });

    } catch (error) {
      console.error('Error fetching filing tracker', error);
    } finally {
      setTrackerLoading(false);
    }
  };

  // ================= Subscriptions =================
  const fetchSubscriptions = async () => {
    setSubsLoading(true);
    try {
      const res = await api.get('/api/tracker/subscriptionsbyid');

      // ✅ backend direct LIST bhej raha hai
      setSubscriptions(res.data || []);
    } catch (err) {
      console.error('Error fetching subscriptions', err);
    } finally {
      setSubsLoading(false);
    }
  };











  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  const handleSaveProfile = async () => {
    try {
      const response = await api.put('/profile', editForm);
      setUser(response.data);
      localStorage.setItem('user', JSON.stringify(response.data));
      setShowProfileEdit(false);
      setToast({ show: true, message: 'Profile updated successfully!', type: 'success' });
      setTimeout(() => setToast({ show: false, message: '', type: 'success' }), 3000);
    } catch (error) {
      console.error('Error updating profile:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Failed to update profile. Please try again.';
      setToast({ show: true, message: errorMessage, type: 'error' });
      setTimeout(() => setToast({ show: false, message: '', type: 'error' }), 5000);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    navigate('/search');
  };

  const handleAdvancedSearch = () => {
    navigate('/search');
  };

  const handleViewDetails = (asset) => {
    navigate(`/ip-asset/${asset.id}`);
  };


  // ================= Subscription Handler =================
  const handleSubscribe = async (asset) => {
    try {
      await api.post('/api/tracker/subscribe', {
        externalId: asset.externalId,          // 🔥 MUST
        title: asset.title,
        assetType: asset.assetType,
        jurisdiction: asset.jurisdiction,
        applicationNumber: asset.applicationNumber,
        publicationNumber: asset.publicationNumber,
        applicationDate: asset.applicationDate,
        grantDate: asset.grantDate,
        expiryDate: asset.expiryDate
      });


      // ✅ refresh from backend
      fetchSubscriptions();

      setToast({
        show: true,
        message: 'Asset subscribed successfully!',
        type: 'success'
      });
    } catch (error) {
      setToast({
        show: true,
        message: 'Failed to subscribe asset',
        type: 'error'
      });
    }
  };

  const handleUnsubscribe = async (externalId) => {
    try {
      await api.post('/api/tracker/unsubscribe', {
        externalId: externalId
      });

      fetchSubscriptions(); // refresh list

      setToast({
        show: true,
        message: 'Unsubscribed successfully',
        type: 'success'
      });
    } catch (error) {
      setToast({
        show: true,
        message: 'Failed to unsubscribe',
        type: 'error'
      });
    }
  };

  const handleViewAsset = (ipAsset) => {
    setSelectedAsset(ipAsset);
    setShowAssetModal(true);
  };













  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600 dark:text-gray-400">Loading...</p>
        </div>
      </div>
    );
  }

  if (!user) return null;

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 transition-colors">
      {/* Sidebar */}
      <aside className={`fixed left-0 top-0 h-full bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 shadow-lg transition-all duration-300 z-40 ${sidebarOpen ? 'w-64' : 'w-20'}`}>
        <div className="flex flex-col h-full">
          <div className="p-6 border-b border-gray-200 dark:border-gray-700">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-primary-600 rounded-xl flex items-center justify-center">
                <Lightbulb className="w-6 h-6 text-white" />
              </div>
              {sidebarOpen && (
                <div>
                  <h1 className="text-xl font-light text-gray-900 dark:text-white">GlobalIP</h1>
                  <p className="text-xs text-gray-600 dark:text-gray-400 font-light">User Dashboard</p>
                </div>
              )}
            </div>
          </div>

          <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
            {[
              { id: 'overview', label: 'Overview', icon: <BarChart3 className="w-5 h-5" /> },
              { id: 'search', label: 'IP Search', icon: <VscSearch className="w-5 h-5" /> },
              { id: 'monitoring', label: 'Monitoring', icon: <Activity className="w-5 h-5" /> },
              { id: 'alerts', label: 'Alerts', icon: <VscBell className="w-5 h-5" /> },
              { id: 'subscriptions', label: 'Subscriptions', icon: <BookmarkIcon /> },


            ].map((item) => (
              <button
                key={item.id}
                onClick={() => {
                  if (item.id === 'search') {
                    navigate('/search');
                  } else {
                    setActiveTab(item.id);
                  }
                }}
                className={`w-full flex items-center space-x-3 px-4 py-3 rounded-xl transition-all ${activeTab === item.id ? 'bg-gradient-to-r from-primary-500 to-primary-600 text-white shadow-lg' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'}`}
              >
                {item.icon}
                {sidebarOpen && <span className="font-medium">{item.label}</span>}
              </button>
            ))}
          </nav>

          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="p-4 border-t border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
          >
            {sidebarOpen ? <ChevronLeft className="w-6 h-6 text-gray-600 dark:text-gray-400" /> : <ChevronRight className="w-6 h-6 text-gray-600 dark:text-gray-400" />}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className={`transition-all duration-300 ${sidebarOpen ? 'ml-64' : 'ml-20'}`}>
        <header className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 sticky top-0 z-30 shadow-sm">
          <div className="flex items-center justify-between px-8 py-4">
            <div>
              <h2 className="text-2xl font-light text-gray-900 dark:text-white">Welcome back, {user.firstName} {user.lastName}</h2>
              <p className="text-gray-600 dark:text-gray-400 font-light">Monitor and search global IP intelligence</p>
            </div>

            <div className="flex items-center space-x-4">
              <DarkModeToggle />
              <div className="relative">
                <button className="relative p-2 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors">
                  <Bell className="w-6 h-6" />
                  {notifications.filter(n => !n.read).length > 0 && (
                    <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
                  )}
                </button>
              </div>

              <div className="relative">
                <button
                  onClick={() => setShowProfileMenu(!showProfileMenu)}
                  className="flex items-center space-x-3 p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full transition-colors"
                >
                  <div className="w-9 h-9 bg-primary-600 rounded-full flex items-center justify-center text-white font-medium">
                    {user.firstName?.charAt(0).toUpperCase()}
                  </div>
                </button>

                {showProfileMenu && (
                  <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-200 dark:border-gray-700 py-2">
                    <div className="px-4 py-2 border-b border-gray-200 dark:border-gray-700">
                      <p className="text-sm font-medium text-gray-900 dark:text-white">{user.firstName} {user.lastName}</p>
                      <p className="text-xs text-gray-500 dark:text-gray-400 font-light">{user.email}</p>
                    </div>
                    <button
                      onClick={() => {
                        setShowProfileView(true);
                        setShowProfileMenu(false);
                      }}
                      className="w-full text-left px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors font-light flex items-center space-x-2">
                      <Eye className="w-4 h-4" />
                      <span>View Profile</span>
                    </button>
                    <button
                      onClick={() => {
                        setShowProfileEdit(true);
                        setShowProfileMenu(false);
                      }}
                      className="w-full text-left px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors font-light flex items-center space-x-2">
                      <User className="w-4 h-4" />
                      <span>Edit Profile</span>
                    </button>
                    <button className="w-full text-left px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors font-light">
                      Settings
                    </button>
                    <button
                      onClick={handleLogout}
                      className="w-full text-left px-4 py-2 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors font-light border-t border-gray-200 dark:border-gray-700"
                    >
                      Logout
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </header>

        <main className="p-8">
          {activeTab === 'overview' && (
            <div className="space-y-8">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {[
                  { label: 'Total Searches', value: dashboardStats.totalSearches, icon: <VscSearch className="w-6 h-6" />, color: 'from-blue-500 to-blue-600' },
                  { label: 'Saved Items', value: dashboardStats.savedItems, icon: <BookmarkIcon className="w-6 h-6" />, color: 'from-primary-500 to-primary-600' },
                  { label: 'Active Alerts', value: dashboardStats.activeAlerts, icon: <VscBell className="w-6 h-6" />, color: 'from-accent-500 to-accent-600' },
                  { label: 'Reports', value: dashboardStats.reports, icon: <FileText className="w-6 h-6" />, color: 'from-green-500 to-green-600' }
                ].map((stat, idx) => (
                  <div key={idx} className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6 hover:shadow-xl transition-shadow">
                    <div className="flex items-center justify-between mb-4">
                      <div className={`w-12 h-12 bg-gradient-to-br ${stat.color} rounded-xl flex items-center justify-center text-white shadow-lg`}>
                        {stat.icon}
                      </div>
                    </div>
                    <p className="text-gray-600 dark:text-gray-400 text-sm mb-1">{stat.label}</p>
                    <p className="text-3xl font-light text-gray-900 dark:text-white">{stat.value}</p>
                  </div>
                ))}
              </div>

              <div className="grid lg:grid-cols-2 gap-6">
                <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
                  <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4 flex items-center space-x-2">
                    <Activity className="w-5 h-5 text-primary-600" />
                    <span>Search Activity</span>
                  </h3>
                  <ResponsiveContainer width="100%" height={300}>
                    <AreaChart data={dashboardStats.activityData}>
                      <defs>
                        <linearGradient id="colorSearches" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#1677ff" stopOpacity={0.8} />
                          <stop offset="95%" stopColor="#1677ff" stopOpacity={0} />
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" stroke={theme === 'dark' ? '#374151' : '#e5e7eb'} />
                      <XAxis dataKey="month" stroke={theme === 'dark' ? '#9ca3af' : '#6b7280'} />
                      <YAxis stroke={theme === 'dark' ? '#9ca3af' : '#6b7280'} />
                      <Tooltip contentStyle={{ backgroundColor: theme === 'dark' ? '#1f2937' : '#fff', border: '1px solid #d1d5db' }} />
                      <Area type="monotone" dataKey="searches" stroke="#1677ff" fillOpacity={1} fill="url(#colorSearches)" />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>

                <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
                  <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4 flex items-center space-x-2">
                    <TrendingUp className="w-5 h-5 text-primary-600" />
                    <span>Technology Distribution</span>
                  </h3>
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={dashboardStats.technologyData}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                        outerRadius={80}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        {dashboardStats.technologyData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.color} />
                        ))}
                      </Pie>
                      <Tooltip contentStyle={{ backgroundColor: theme === 'dark' ? '#1f2937' : '#fff', border: '1px solid #d1d5db' }} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'search' && (
            <div className="space-y-6">
              <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
                <h3 className="text-xl font-medium text-gray-900 dark:text-white mb-6 flex items-center space-x-2">
                  <Search className="w-6 h-6 text-primary-600" />
                  <span>IP Search</span>
                </h3>
                <form onSubmit={handleSearch} className="space-y-4">
                  <div className="flex gap-4">
                    <select
                      value={searchType}
                      onChange={(e) => setSearchType(e.target.value)}
                      className="px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 dark:focus:ring-primary-400"
                    >
                      <option value="patents">Patents</option>
                      <option value="trademarks">Trademarks</option>
                    </select>
                    <input
                      type="text"
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      placeholder="Search by keyword, assignee, inventor..."
                      className="flex-1 px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 focus:ring-2 focus:ring-primary-500 dark:focus:ring-primary-400"
                    />
                    <button type="submit" className="px-8 py-3 bg-gradient-to-r from-primary-600 to-accent-600 text-white rounded-xl font-semibold hover:shadow-lg transition-all flex items-center space-x-2">
                      <Search className="w-5 h-5" />
                      <span>Search</span>
                    </button>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                    <select
                      value={searchFilters.jurisdiction}
                      onChange={(e) => setSearchFilters({ ...searchFilters, jurisdiction: e.target.value })}
                      className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                    >
                      <option value="">All Jurisdictions</option>
                      <option value="US">United States</option>
                      <option value="EP">Europe</option>
                      <option value="CN">China</option>
                    </select>
                    <select
                      value={searchFilters.technology}
                      onChange={(e) => setSearchFilters({ ...searchFilters, technology: e.target.value })}
                      className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                    >
                      <option value="">All Technologies</option>
                      <option value="AI">AI/ML</option>
                      <option value="Blockchain">Blockchain</option>
                      <option value="IoT">IoT</option>
                    </select>
                    <input
                      type="date"
                      value={searchFilters.dateFrom}
                      onChange={(e) => setSearchFilters({ ...searchFilters, dateFrom: e.target.value })}
                      className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                    />
                    <input
                      type="date"
                      value={searchFilters.dateTo}
                      onChange={(e) => setSearchFilters({ ...searchFilters, dateTo: e.target.value })}
                      className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                    />
                  </div>
                </form>
              </div>

              <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-lg border border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-medium text-gray-900 dark:text-white">Quick Search Results</h3>
                  <button
                    onClick={handleAdvancedSearch}
                    className="text-sm text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 font-medium"
                  >
                    Advanced Search →
                  </button>
                </div>
                {searchLoading ? (
                  <div className="flex items-center justify-center py-12">
                    <div className="text-center">
                      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary-600 mx-auto mb-3"></div>
                      <p className="text-sm text-gray-600 dark:text-gray-400">Searching...</p>
                    </div>
                  </div>
                ) : searchResults.length > 0 ? (
                  <div className="space-y-4">
                    {searchResults.map((result) => (
                      <div
                        key={result.id}
                        onClick={() => handleViewDetails(result)}
                        className="p-4 border border-gray-200 dark:border-gray-700 rounded-xl hover:border-primary-500 dark:hover:border-primary-600 transition-all cursor-pointer"
                      >
                        <div className="flex items-start justify-between mb-2">
                          <div className="flex-1">
                            <div className="flex items-center space-x-2 mb-2">
                              <span className={`px-2 py-1 rounded text-xs font-medium ${result.assetType === 'PATENT'
                                ? 'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300'
                                : 'bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300'
                                }`}>
                                {result.assetType}
                              </span>
                              <span className="text-sm text-primary-600 dark:text-primary-400 font-medium">
                                {result.publicationNumber || result.applicationNumber}
                              </span>
                            </div>
                            <h4 className="font-semibold text-gray-900 dark:text-white mb-1 line-clamp-2">{result.title}</h4>
                            {result.description && (
                              <p className="text-sm text-gray-600 dark:text-gray-400 line-clamp-2 mb-2">{result.description}</p>
                            )}
                            <div className="flex flex-wrap gap-2 text-sm text-gray-600 dark:text-gray-400">
                              {result.assignee && <span>Assignee: {result.assignee}</span>}
                              {result.assignee && result.applicationDate && <span>•</span>}
                              {result.applicationDate && (
                                <span>Filed: {new Date(result.applicationDate).toLocaleDateString()}</span>
                              )}
                              {result.status && (
                                <>
                                  <span>•</span>
                                  <span className="px-2 py-0.5 bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300 rounded">
                                    {result.status}
                                  </span>
                                </>
                              )}
                            </div>
                          </div>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleSubscribe(result);
                            }}
                            className={`ml-4 p-2 rounded-lg transition-colors ${subscribedIds.includes(result.id)
                              ? 'text-green-600 bg-green-100 dark:bg-green-900/30'
                              : 'text-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700'
                              }`}
                            title="Subscribe / Track"
                          >
                            <BookmarkIcon className="w-5 h-5" />
                          </button>

                        </div>
                      </div>
                    ))}
                    <button
                      onClick={handleAdvancedSearch}
                      className="w-full py-3 text-center text-primary-600 dark:text-primary-400 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg transition-colors font-medium"
                    >
                      View all results →
                    </button>
                  </div>
                ) : (
                  <div className="text-center py-12">
                    <Search className="w-12 h-12 text-gray-400 mx-auto mb-3" />
                    <p className="text-gray-600 dark:text-gray-400">
                      {searchQuery ? 'No results found. Try different keywords.' : 'Enter search terms above to get started'}
                    </p>
                  </div>
                )}
              </div>

              {/* Search History */}
              <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-lg border border-gray-200 dark:border-gray-700">
                <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">Recent Searches</h3>
                {historyLoading ? (
                  <div className="text-center py-8">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
                  </div>
                ) : searchHistory.length > 0 ? (
                  <div className="space-y-3">
                    {searchHistory.slice(0, 5).map((history, idx) => (
                      <div
                        key={idx}
                        className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors cursor-pointer"
                        onClick={() => {
                          setSearchQuery(history.searchQuery || '');
                          setActiveTab('search');
                        }}
                      >
                        <div className="flex-1">
                          <p className="text-sm font-medium text-gray-900 dark:text-white">
                            {history.searchQuery || 'General search'}
                          </p>
                          <p className="text-xs text-gray-500 dark:text-gray-400">
                            {new Date(history.searchDate).toLocaleDateString()} • {history.resultsCount || 0} results
                          </p>
                        </div>
                        <ChevronRight className="w-4 h-4 text-gray-400" />
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-center text-gray-600 dark:text-gray-400 py-8">No search history yet</p>
                )}
              </div>
            </div>
          )}


          {activeTab === 'monitoring' && (
            <div className="space-y-8">

              {/* Legal Status Summary */}
              <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                {[
                  { label: 'Applications', value: statusStats.application, color: 'from-blue-500 to-blue-600' },
                  { label: 'Granted', value: statusStats.granted, color: 'from-green-500 to-green-600' },
                  { label: 'Expired', value: statusStats.expired, color: 'from-red-500 to-red-600' },
                  { label: 'Renewals', value: statusStats.renewal, color: 'from-yellow-500 to-yellow-600' }
                ].map((item, idx) => (
                  <div key={idx} className="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow border">
                    <div className={`w-10 h-10 bg-gradient-to-br ${item.color} rounded-lg mb-3`} />
                    <p className="text-sm text-gray-500">{item.label}</p>
                    <p className="text-2xl font-light text-gray-900 dark:text-white">{item.value}</p>
                  </div>
                ))}
              </div>

              {/* Filing Tracker Table */}
              <div className="bg-white dark:bg-gray-800 rounded-2xl shadow border p-6">
                <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">
                  My Filing Tracker
                </h3>

                {trackerLoading ? (
                  <p className="text-gray-500">Loading tracked filings...</p>
                ) : trackedAssets.length > 0 ? (
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="text-left text-gray-500 border-b">
                        <th className="py-2">Title</th>
                        <th>Status</th>
                        <th>Jurisdiction</th>
                        <th>Filed On</th>
                      </tr>
                    </thead>
                    <tbody>
                      {trackedAssets.map(asset => (
                        <tr key={asset.id} className="border-b hover:bg-gray-50 dark:hover:bg-gray-700">
                          <td className="py-2 font-medium">{asset.title}</td>
                          <td>
                            <span className="px-2 py-1 rounded bg-green-100 text-green-700">
                              {asset.status}
                            </span>
                          </td>
                          <td>{asset.jurisdiction}</td>
                          <td>
                            {asset.filingDate
                              ? new Date(asset.filingDate + 'T00:00:00').toLocaleDateString()
                              : '-'}
                          </td>

                        </tr>
                      ))}
                    </tbody>
                  </table>
                ) : (
                  <p className="text-gray-500">No tracked filings yet</p>
                )}
              </div>

              {/* Status Pie Chart */}
              <div className="bg-white dark:bg-gray-800 rounded-2xl shadow border p-6">
                <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">
                  Filing Status Distribution
                </h3>

                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={[
                        { name: 'Application', value: statusStats.application },
                        { name: 'Granted', value: statusStats.granted },
                        { name: 'Expired', value: statusStats.expired },
                        { name: 'Renewal', value: statusStats.renewal }
                      ]}
                      dataKey="value"
                      outerRadius={100}
                      label
                    >
                      <Cell fill="#3b82f6" />
                      <Cell fill="#22c55e" />
                      <Cell fill="#ef4444" />
                      <Cell fill="#facc15" />
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </div>

            </div>
          )}



          {activeTab === 'subscriptions' && (
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow border p-6">
              <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">
                My Subscribed IP Assets
              </h3>



              {subsLoading ? (
                <p className="text-gray-500">Loading subscriptions...</p>
              ) : subscriptions.length > 0 ? (
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-left text-gray-500 border-b">
                      <th className="py-2">Title</th>
                      <th>Status</th>
                      <th>Jurisdiction</th>
                      <th>Filed On</th>
                      <th className="py-2">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {subscriptions.map(sub => (
                      <tr key={sub.id} className="border-b hover:bg-gray-50 dark:hover:bg-gray-700">

                        <td className="py-2 font-medium">
                          {sub.ipAsset?.title}
                        </td>

                        <td>
                          <span className="px-2 py-1 rounded bg-green-100 text-green-700">
                            {sub.ipAsset?.status}
                          </span>
                        </td>

                        <td>{sub.ipAsset?.jurisdiction}</td>

                        <td>
                          {sub.ipAsset?.applicationDate
                            ? new Date(sub.ipAsset.applicationDate).toLocaleDateString()
                            : '-'}
                        </td>

                        {/* ACTION BUTTONS */}
                        <td className="flex gap-2 py-2">

                          {/* VIEW DETAILS */}
                          <button
                            onClick={() => handleViewAsset(sub.ipAsset)}
                            className="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 text-xs"
                          >
                            View
                          </button>

                          {/* UNSUBSCRIBE */}
                          <button
                            onClick={() => handleUnsubscribe(sub.ipAsset.externalId)}
                            className="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700 text-xs"
                          >
                            Unsubscribe
                          </button>

                        </td>
                      </tr>
                    ))}
                  </tbody>

                </table>
              ) : (
                <p className="text-gray-500">No subscriptions yet</p>
              )}
            </div>
          )}






          {activeTab === 'alerts' && (
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
              <h3 className="text-xl font-medium text-gray-900 dark:text-white mb-6 flex items-center space-x-2">
                <Bell className="w-6 h-6 text-primary-600" />
                <span>Recent Alerts</span>
              </h3>
              <div className="space-y-3">
                {notifications.map((notif) => (
                  <div key={notif.id} className={`p-4 border rounded-xl transition-all ${notif.read ? 'border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700/50' : 'border-primary-200 dark:border-primary-800 bg-primary-50 dark:bg-primary-900/20'}`}>
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <h4 className="font-bold text-gray-900 dark:text-white mb-1">{notif.title}</h4>
                        <p className="text-sm text-gray-600 dark:text-gray-400 mb-1">{notif.message}</p>
                        <p className="text-xs text-gray-500 dark:text-gray-500">{notif.time}</p>
                      </div>
                      <button className="ml-4 p-2 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors">
                        <X className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </main>
      </div>

      {/* Profile Edit Modal */}
      {showProfileEdit && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl max-w-md w-full p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-2xl font-semibold text-gray-900 dark:text-white">Edit Profile</h3>
              <button
                onClick={() => setShowProfileEdit(false)}
                className="p-2 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  First Name
                </label>
                <input
                  type="text"
                  value={editForm.firstName}
                  onChange={(e) => setEditForm({ ...editForm, firstName: e.target.value })}
                  className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all"
                  placeholder="Enter first name"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Last Name
                </label>
                <input
                  type="text"
                  value={editForm.lastName}
                  onChange={(e) => setEditForm({ ...editForm, lastName: e.target.value })}
                  className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all"
                  placeholder="Enter last name"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Email
                </label>
                <input
                  type="email"
                  value={editForm.email}
                  onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                  className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all"
                  placeholder="Enter email"
                />
              </div>

              <div className="flex space-x-3 pt-4">
                <button
                  onClick={() => setShowProfileEdit(false)}
                  className="flex-1 px-4 py-3 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors font-medium"
                >
                  Cancel
                </button>
                <button
                  onClick={handleSaveProfile}
                  className="flex-1 px-4 py-3 bg-primary-600 text-white rounded-xl hover:bg-primary-700 transition-colors font-medium"
                >
                  Save Changes
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Profile View Modal */}
      {showProfileView && user && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl max-w-md w-full p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-2xl font-semibold text-gray-900 dark:text-white">Profile</h3>
              <button
                onClick={() => setShowProfileView(false)}
                className="p-2 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-6">
              {/* Profile Avatar */}
              <div className="flex justify-center">
                <div className="w-24 h-24 bg-primary-600 rounded-full flex items-center justify-center text-white text-3xl font-medium">
                  {user.firstName?.charAt(0).toUpperCase()}
                </div>
              </div>

              {/* Profile Information */}
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">
                    Full Name
                  </label>
                  <p className="text-lg text-gray-900 dark:text-white font-medium">
                    {user.firstName} {user.lastName}
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">
                    Email Address
                  </label>
                  <p className="text-lg text-gray-900 dark:text-white">
                    {user.email}
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">
                    Username
                  </label>
                  <p className="text-lg text-gray-900 dark:text-white">
                    {user.username}
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">
                    Role
                  </label>
                  <span className="inline-flex px-3 py-1 rounded-full text-sm font-medium bg-primary-100 dark:bg-primary-900/30 text-primary-700 dark:text-primary-300">
                    {user.role}
                  </span>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex space-x-3 pt-4">
                <button
                  onClick={() => setShowProfileView(false)}
                  className="flex-1 px-4 py-3 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors font-medium"
                >
                  Close
                </button>
                <button
                  onClick={() => {
                    setShowProfileView(false);
                    setShowProfileEdit(true);
                  }}
                  className="flex-1 px-4 py-3 bg-primary-600 text-white rounded-xl hover:bg-primary-700 transition-colors font-medium"
                >
                  Edit Profile
                </button>
              </div>
            </div>
          </div>
        </div>
      )}



      {showAssetModal && selectedAsset && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl max-w-2xl w-full p-6 overflow-y-auto max-h-[90vh]">

            {/* Header */}
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">
                IP Asset Details
              </h3>
              <button
                onClick={() => setShowAssetModal(false)}
                className="text-gray-500 hover:text-gray-700"
              >
                <X />
              </button>
            </div>

            {/* Content */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">

              <Detail label="Title" value={selectedAsset.title} />
              <Detail label="Asset Type" value={selectedAsset.assetType} />
              <Detail label="Status" value={selectedAsset.status} />
              <Detail label="Jurisdiction" value={selectedAsset.jurisdiction} />
              <Detail label="Patent Office" value={selectedAsset.patentOffice} />

              <Detail label="Application No" value={selectedAsset.applicationNumber} />
              <Detail label="Publication No" value={selectedAsset.publicationNumber} />

              <Detail label="Inventor" value={selectedAsset.inventor} />
              <Detail label="Assignee" value={selectedAsset.assignee} />

              <Detail label="Priority Date" value={selectedAsset.priorityDate} />
              <Detail label="Application Date" value={selectedAsset.applicationDate} />
              <Detail label="Publication Date" value={selectedAsset.publicationDate} />
              <Detail label="Grant Date" value={selectedAsset.grantDate} />
              <Detail label="Expiry Date" value={selectedAsset.expiryDate} />

              <Detail label="IPC Class" value={selectedAsset.ipcClassification} />
              <Detail label="CPC Class" value={selectedAsset.cpcClassification} />
              <Detail label="Keywords" value={selectedAsset.keywords} />

            </div>

            {/* Footer */}
            <div className="mt-6 text-right">
              <button
                onClick={() => setShowAssetModal(false)}
                className="px-4 py-2 bg-primary-600 text-white rounded hover:bg-primary-700"
              >
                Close
              </button>
            </div>

          </div>
        </div>
      )}








      {/* Toast Notification */}
      {toast.show && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast({ show: false, message: '', type: 'success' })}
        />
      )}
    </div>
  );
}
