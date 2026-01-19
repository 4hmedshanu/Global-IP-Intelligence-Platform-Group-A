import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users, Activity, Server, Database, AlertCircle, CheckCircle, Clock, TrendingUp, Lightbulb, ChevronLeft, ChevronRight, Settings, User, Eye } from 'lucide-react';
import { VscServerProcess, VscDatabase, VscAccount } from 'react-icons/vsc';
import { LineChart, Line, AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { useTheme } from '../context/ThemeContext';
import DarkModeToggle from './DarkModeToggle';
import api from '../api/axios';

export default function AdminDashboard() {
  const navigate = useNavigate();
  const { theme } = useTheme();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [showProfileMenu, setShowProfileMenu] = useState(false);



  // Dashboard data state (dynamic)
  const [dashboardData, setDashboardData] = useState({
    systemUsage: [],
    apiHealth: [],
    recentLogs: [],
    dbMetrics: {},
    users: []
  });
  // 👇 YAHAN ADD
  const [users, setUsers] = useState([]);
  const [apiHealth, setApiHealth] = useState([]);
  const [logs, setLogs] = useState([]);
  const [dbMetrics, setDbMetrics] = useState({});


  // USERS API
  const fetchUsers = async () => {
    const res = await api.get('/api/admin/users');
    setUsers(res.data);
  };


  // DELETE USER (soft delete)
  const deleteUser = async (id) => {
    await api.delete(`/api/admin/users/${id}`);
    fetchUsers(); // refresh list
  };

  // VERIFY ADMIN
  const verifyAdmin = async (id) => {
    await api.put(`/api/admin/users/${id}/verify`);
    fetchUsers(); // refresh list
  };





  // API HEALTH
  const fetchApiHealth = async () => {
    const res = await api.get('/api/admin/api-health');
    setApiHealth(res.data);
  };

  // LOGS
  const fetchLogs = async () => {
    const res = await api.get('/api/admin/logs');
    setLogs(res.data);
  };

  // DB METRICS
  const fetchDbMetrics = async () => {
    const res = await api.get('/api/admin/db-metrics');
    setDbMetrics(res.data);
  };





  useEffect(() => {
    const fetchProfile = async () => {
      const token = localStorage.getItem('token');
      const userData = localStorage.getItem('user');
      if (!token || !userData) {
        setLoading(false);
        navigate('/login');
        return;
      }
      setUser(JSON.parse(userData));
      setLoading(false);
      try {
        const response = await api.get('/api/profile');
        setUser(response.data);
        localStorage.setItem('user', JSON.stringify(response.data));
      } catch (error) {
        console.error('Error fetching profile (using cached data):', error);
      }
    };
    fetchProfile();
  }, [navigate]);

  useEffect(() => {
    if (activeTab === 'users') fetchUsers();
    if (activeTab === 'api-health') fetchApiHealth();
    if (activeTab === 'logs') fetchLogs();
    if (activeTab === 'database') fetchDbMetrics();
  }, [activeTab]);


 useEffect(() => {
  const fetchDashboard = async () => {
    try {
      const res = await api.get('/api/admin/dashboard/admin');

      console.log('DASHBOARD RESPONSE 👉', res.data);

      setDashboardData({
        systemUsage: res.data.systemStats?.[0]?.systemUsage || [],
        apiHealth: [],
        recentLogs: [],
        dbMetrics: {},
        users: []
      });

    } catch (err) {
      console.error('Dashboard error:', err);
    }
  };

  fetchDashboard();
}, []);






  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
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

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 transition-colors">
      {/* Sidebar */}
      <aside className={`fixed left-0 top-0 h-full bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 shadow-lg transition-all duration-300 z-40 ${sidebarOpen ? 'w-64' : 'w-20'}`}>
        <div className="flex flex-col h-full">
          <div className="p-6 border-b border-gray-200 dark:border-gray-700">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-primary-600 rounded-xl flex items-center justify-center">
                <Settings className="w-6 h-6 text-white" />
              </div>
              {sidebarOpen && (
                <div>
                  <h1 className="text-xl font-light text-gray-900 dark:text-white">GlobalIP</h1>
                  <p className="text-xs text-gray-600 dark:text-gray-400 font-light">Admin Control</p>
                </div>
              )}
            </div>
          </div>

          <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
            {[
              { id: 'overview', label: 'Overview', icon: <Activity className="w-5 h-5" /> },
              { id: 'users', label: 'Users', icon: <VscAccount className="w-5 h-5" /> },
              { id: 'api-health', label: 'API Health', icon: <VscServerProcess className="w-5 h-5" /> },
              { id: 'logs', label: 'System Logs', icon: <Server className="w-5 h-5" /> },
              { id: 'database', label: 'Database', icon: <VscDatabase className="w-5 h-5" /> }
            ].map((item) => (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`w-full flex items-center space-x-3 px-4 py-3 rounded-xl transition-all ${activeTab === item.id ? 'bg-gradient-to-r from-primary-500 to-accent-500 text-white shadow-lg' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'}`}
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
              <h2 className="text-2xl font-light text-gray-900 dark:text-white">System Administration</h2>
              <p className="text-gray-600 dark:text-gray-400 font-light">Platform monitoring and management</p>
            </div>

            <div className="flex items-center space-x-4">
              <DarkModeToggle />
              <div className="relative">
                <button className="relative p-2 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors">
                  <AlertCircle className="w-6 h-6" />
                  <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
                </button>
              </div>

              <div className="relative">
                <button
                  onClick={() => setShowProfileMenu(!showProfileMenu)}
                  className="flex items-center space-x-3 p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full transition-colors"
                >
                  <div className="w-9 h-9 bg-primary-600 rounded-full flex items-center justify-center text-white font-medium">
                    {user?.firstName?.charAt(0).toUpperCase() || 'A'}
                  </div>
                </button>

                {showProfileMenu && (
                  <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-200 dark:border-gray-700 py-2">
                    <div className="px-4 py-2 border-b border-gray-200 dark:border-gray-700">
                      <p className="text-sm font-medium text-gray-900 dark:text-white">{user?.firstName} {user?.lastName}</p>
                      <p className="text-xs text-gray-500 dark:text-gray-400 font-light">{user?.email || 'admin@globalip.com'}</p>
                    </div>
                    <button className="w-full text-left px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors font-light flex items-center space-x-2">
                      <Eye className="w-4 h-4" />
                      <span>View Profile</span>
                    </button>
                    <button className="w-full text-left px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors font-light flex items-center space-x-2">
                      <User className="w-4 h-4" />
                      <span>Edit Profile</span>
                    </button>
                    <button className="w-full text-left px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors font-light">
                      System Settings
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
           

              <div className="grid lg:grid-cols-2 gap-6">
                <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
                  <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4 flex items-center space-x-2">
                    <TrendingUp className="w-5 h-5 text-red-600" />
                    <span>System Usage Trends</span>
                  </h3>
                  <ResponsiveContainer width="100%" height={300}>
                    <AreaChart data={dashboardData.systemUsage}>
                      <defs>
                        <linearGradient id="colorUsers" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#1677ff" stopOpacity={0.8} />
                          <stop offset="95%" stopColor="#1677ff" stopOpacity={0} />
                        </linearGradient>
                        <linearGradient id="colorAPI" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#ef4444" stopOpacity={0.8} />
                          <stop offset="95%" stopColor="#ef4444" stopOpacity={0} />
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" stroke={theme === 'dark' ? '#374151' : '#e5e7eb'} />
                      <XAxis dataKey="date" stroke={theme === 'dark' ? '#9ca3af' : '#6b7280'} />
                      <YAxis stroke={theme === 'dark' ? '#9ca3af' : '#6b7280'} />
                      <Tooltip contentStyle={{ backgroundColor: theme === 'dark' ? '#1f2937' : '#fff', border: '1px solid #d1d5db' }} />
                      <Legend />
                      <Area type="monotone" dataKey="users" stroke="#1677ff" fillOpacity={1} fill="url(#colorUsers)" />
                      <Area type="monotone" dataKey="apiCalls" stroke="#ef4444" fillOpacity={1} fill="url(#colorAPI)" />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>

                <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
                  <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4 flex items-center space-x-2">
                    <Database className="w-5 h-5 text-red-600" />
                    <span>Database Metrics</span>
                  </h3>
                  <div className="space-y-4">
                    {Object.keys(dbMetrics).length === 0 ? (
                      <p className="text-gray-500 text-sm">No database metrics available</p>
                    ) : (
                      Object.entries(dbMetrics).map(([key, value]) => (
                        <div
                          key={key}
                          className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700/50 rounded-xl"
                        >
                          <div className="flex items-center space-x-3">
                            <Database className="w-5 h-5 text-blue-600" />
                            <span className="text-gray-700 dark:text-gray-300 font-medium">
                              {key}
                            </span>
                          </div>
                          <span className="text-gray-900 dark:text-white font-bold">
                            {value}
                          </span>
                        </div>
                      ))
                    )}
                  </div>

                </div>
              </div>
            </div>
          )}

          {activeTab === 'api-health' && (
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
              <h3 className="text-xl font-medium text-gray-900 dark:text-white mb-6 flex items-center space-x-2">
                <VscServerProcess className="w-6 h-6 text-red-600" />
                <span>API Health Status</span>
              </h3>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-gray-200 dark:border-gray-700">
                      <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Endpoint</th>
                      <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Status</th>
                      <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Uptime</th>
                      <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Avg Response</th>
                    </tr>
                  </thead>
                  <tbody>
                    {apiHealth.map((api, idx) => (
                      <tr key={idx} className="border-b border-gray-100 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
                        <td className="py-4 px-4 font-mono text-sm text-gray-900 dark:text-white">{api.endpoint}</td>
                        <td className="py-4 px-4">
                          <div className="flex items-center space-x-2">
                            <CheckCircle className="w-5 h-5 text-green-500" />
                            <span className="text-green-600 dark:text-green-400 font-semibold">{api.status}</span>
                          </div>
                        </td>
                        <td className="py-4 px-4 text-gray-600 dark:text-gray-400">{api.uptime}</td>
                        <td className="py-4 px-4 text-gray-600 dark:text-gray-400">{api.avgResponse}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {activeTab === 'logs' && (
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
              <h3 className="text-xl font-medium text-gray-900 dark:text-white mb-6 flex items-center space-x-2">
                <Server className="w-6 h-6 text-red-600" />
                <span>System Logs</span>
              </h3>
              <div className="space-y-2">
                {logs.map((log) => (
                  <div key={log.id} className={`p-4 border-l-4 rounded-r-lg ${log.level === 'ERROR' ? 'border-red-500 bg-red-50 dark:bg-red-900/20' :
                    log.level === 'WARN' ? 'border-yellow-500 bg-yellow-50 dark:bg-yellow-900/20' :
                      'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                    }`}>
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3 mb-1">
                          <span className={`px-2 py-1 rounded text-xs font-bold ${log.level === 'ERROR' ? 'bg-red-100 dark:bg-red-900/50 text-red-700 dark:text-red-300' :
                            log.level === 'WARN' ? 'bg-yellow-100 dark:bg-yellow-900/50 text-yellow-700 dark:text-yellow-300' :
                              'bg-blue-100 dark:bg-blue-900/50 text-blue-700 dark:text-blue-300'
                            }`}>
                            {log.level}
                          </span>
                          <span className="text-gray-600 dark:text-gray-400 text-sm font-semibold">{log.service}</span>
                          <span className="text-gray-500 dark:text-gray-500 text-xs">{log.ip}</span>
                        </div>
                        <p className="text-gray-900 dark:text-white text-sm mb-1">{log.message}</p>
                        <p className="text-gray-500 dark:text-gray-500 text-xs">{log.timestamp}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'users' && (
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
              <h3 className="text-xl font-medium text-gray-900 dark:text-white mb-6 flex items-center space-x-2">
                <Users className="w-6 h-6 text-red-600" />
                <span>User Management</span>
              </h3>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-gray-200 dark:border-gray-700">
                      <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Username</th>
                      <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Email</th>
                      <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Role</th>
                      <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Status</th>
                      <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Actions</th>


                    </tr>
                  </thead>
                  <tbody>
                    {users.map((user) => (
                      <tr key={user.id} className="border-b border-gray-100 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
                        <td className="py-4 px-4 font-medium text-gray-900 dark:text-white">{user.username}</td>
                        <td className="py-4 px-4 text-gray-600 dark:text-gray-400">{user.email}</td>
                        <td className="py-4 px-4">
                          <span className={`px-3 py-1 rounded-full text-xs font-semibold ${user.role === 'ADMIN' ? 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300' :
                            user.role === 'ANALYST' ? 'bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300' :
                              'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300'
                            }`}>
                            {user.role}
                          </span>
                        </td>
                        <td className="py-4 px-4">
                          <span className={`px-3 py-1 rounded-full text-xs font-semibold ${user.status === 'Active' ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300' :
                            'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400'
                            }`}>
                            {user.status}
                          </span>
                        </td>

                        <td className="py-4 px-4 space-x-2">

                          {/* DELETE USER */}
                          <button
                            onClick={() => deleteUser(user.id)}
                            className="px-3 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600"
                          >
                            Delete
                          </button>

                          {/* VERIFY ADMIN */}
                          {user.role === 'ADMIN' && !user.verified && (
                            <button
                              onClick={() => verifyAdmin(user.id)}
                              className="px-3 py-1 bg-green-500 text-white rounded text-xs hover:bg-green-600"
                            >
                              Verify
                            </button>
                          )}

                        </td>







                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>







          )
          }


          {activeTab === 'database' && (
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
              <h3 className="text-xl font-medium text-gray-900 dark:text-white mb-6 flex items-center space-x-2">
                <Database className="w-6 h-6 text-red-600" />
                <span>Database Metrics</span>
              </h3>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {Object.keys(dbMetrics).length === 0 ? (
                  <p className="text-gray-500 text-sm">No database metrics available</p>
                ) : (
                  Object.entries(dbMetrics).map(([key, value]) => (
                    <div
                      key={key}
                      className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700/50 rounded-xl"
                    >
                      <div className="flex items-center space-x-3">
                        <Database className="w-5 h-5 text-blue-600" />
                        <span className="text-gray-700 dark:text-gray-300 font-medium">
                          {key}
                        </span>
                      </div>
                      <span className="text-gray-900 dark:text-white font-bold">
                        {value}
                      </span>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}






        </main>
      </div>
    </div>
  );
}
