import jsPDF from 'jspdf';
import 'jspdf-autotable';
import html2canvas from 'html2canvas';
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { BarChart3, TrendingUp, Target, FileText, Eye, Globe, Bell, Activity } from 'lucide-react';
import { PieChart as PieIcon } from 'lucide-react';
import {
  Search,
  ChevronLeft,
  ChevronRight,
  AlertCircle,
  Pause,
  Play
} from 'lucide-react';

import { VscGraphLine } from 'react-icons/vsc';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, AreaChart, Area, Pie, PieChart } from 'recharts';
import { useTheme } from '../context/ThemeContext';
import DarkModeToggle from './DarkModeToggle';
import api from '../api/axios';
// Naye imports add karein
import { X, BellRing, Trash2 } from 'lucide-react'; // Lucide icons for UI





function DynamicBadge({ label, value, gradient }) {
  const [displayValue, setDisplayValue] = React.useState(0);
  useEffect(() => {
    if (typeof value === 'number') {
      setDisplayValue(value);
    } else {
      setDisplayValue(0);
    }
  }, [value]);
  return (
    <div className={`flex items-center space-x-4 bg-gradient-to-r ${gradient} rounded-2xl shadow-lg px-8 py-5 w-full`}>
      <BarChart3 className="w-10 h-10 text-white drop-shadow-lg" />
      <div className="flex flex-col items-start">
        <span className={`text-lg ${gradient.includes('indigo') ? 'text-indigo-100' : 'text-blue-100'} font-semibold tracking-wide`}>{label}</span>
        <span className="text-4xl font-extrabold text-white leading-tight drop-shadow-lg">
          {displayValue}
        </span>
      </div>
    </div>
  );
}

export default function AnalystDashboard() {
  const navigate = useNavigate();
  const { theme } = useTheme();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('analytics');
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  const [showSubscriptionModal, setShowSubscriptionModal] = useState(false);
  const [selectedAssetForFilings, setSelectedAssetForFilings] = useState(null);
  const [notifications, setNotifications] = useState([]); // WebSocket alerts store karne ke liye
  const [filingHistory, setFilingHistory] = useState([]);
  const [showReportModal, setShowReportModal] = useState(false);
  const [viewAsset, setViewAsset] = useState(null);
  // 🔥 ADD THIS
  const [selectedLifecycle, setSelectedLifecycle] = useState([]);
  const [selectedLifecycleAsset, setSelectedLifecycleAsset] = useState(null);

  const [lifecycleStats, setLifecycleStats] = useState({
    APPLICATION: 0,
    GRANTED: 0,
    RENEWAL: 0,
    EXPIRED: 0
  });

  const LIFECYCLE_ORDER = [
    'APPLICATION',
    'GRANTED',
    'RENEWAL',
    'EXPIRED'
  ];







  // Dynamic navigation buttons (default fallback)
  const defaultNav = [
    { id: 'analytics', label: 'Analytics', icon: <BarChart3 className="w-5 h-5" /> },
    { id: 'ipsearch', label: 'IP Search', icon: <Search className="w-5 h-5" />, isRoute: true },
    { id: 'trends', label: 'Trends', icon: <TrendingUp className="w-5 h-5" /> },
    { id: 'competitors', label: 'Competitors', icon: <Target className="w-5 h-5" /> },
    { id: 'subscriptions', label: 'Subscriptions', icon: <Bell className="w-5 h-5" /> },
    { id: 'reports', label: 'Reports', icon: <FileText className="w-5 h-5" /> }
  ];
  const [navButtons, setNavButtons] = useState(defaultNav);


  const [dashboardData, setDashboardData] = useState({
    analyticsData: [],
    trendData: [],
    techPieData: [],
    citationData: [],
    familyData: [],
    competitorActivity: [],
    subscriptions: [],
    statCards: []
  });


  // Filter state
  const [filters, setFilters] = useState({
    jurisdiction: '',
    technology: '',
    fromDate: '',
    toDate: ''
  });


  const handleViewAsset = (asset) => {
    setViewAsset(asset);
  };

  // 🔥 ADD THIS FUNCTION
  const loadLifecycle = async (assetId, asset) => {
    try {
      const res = await api.get(`/api/tracker/lifecycle/${assetId}`);
      console.log("Lifecycle:", res.data); // 👈 debug
      setSelectedLifecycle(res.data || []);
      setSelectedLifecycleAsset(asset);
    } catch (err) {
      console.error(err);
      alert("Lifecycle not available for this asset");
    }
  };



  const exportPDF = async () => {
    const input = document.getElementById('report-content');
    if (!input) return;

    const canvas = await html2canvas(input, {
      scale: 2,
      useCORS: true
    });

    const imgData = canvas.toDataURL('image/png');
    const pdf = new jsPDF('p', 'mm', 'a4');

    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfHeight = (canvas.height * pdfWidth) / canvas.width;

    let position = 0;
    let heightLeft = pdfHeight;

    pdf.addImage(imgData, 'PNG', 0, position, pdfWidth, pdfHeight);
    heightLeft -= pdf.internal.pageSize.getHeight();

    while (heightLeft > 0) {
      position = heightLeft - pdfHeight;
      pdf.addPage();
      pdf.addImage(imgData, 'PNG', 0, position, pdfWidth, pdfHeight);
      heightLeft -= pdf.internal.pageSize.getHeight();
    }

    pdf.save('IP_Intelligence_Report.pdf');
  };


  const exportCSV = () => {
    const headers = [
      'Company',
      'Filings',
      'Granted',
      'Pending'
    ];

    const rows = dashboardData.competitorActivity.map(c => [
      c.company,
      c.filings,
      c.grants,
      c.pending
    ]);

    let csvContent =
      headers.join(',') +
      '\n' +
      rows.map(e => e.join(',')).join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = 'Competitor_Report.csv';
    link.click();

    URL.revokeObjectURL(url);
  };








  // Fetch dashboard data with filters
  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        // Send filters as query params
        const params = {};
        if (filters.jurisdiction) params.jurisdiction = filters.jurisdiction;
        if (filters.technology) params.technology = filters.technology;
        if (filters.fromDate) params.fromDate = filters.fromDate;
        if (filters.toDate) params.toDate = filters.toDate;
        const res = await api.get('/api/dashboard/analyst', { params });
        if (res.data.navButtons && Array.isArray(res.data.navButtons)) {
          setNavButtons(res.data.navButtons);
        } else {
          setNavButtons(defaultNav);
        }
        let statCards = [];
        // Always show search count stat cards, use totalSearches for total, and patent/trademark for others
        statCards.push({ label: 'Total Searches', value: typeof res.data.totalSearches === 'number' ? res.data.totalSearches : 0, change: '', icon: <BarChart3 className="w-6 h-6" />, color: 'from-blue-500 to-blue-600' });
        statCards.push({ label: 'Patent Searches', value: typeof res.data.patentSearchCount === 'number' ? res.data.patentSearchCount : 0, change: '', icon: <BarChart3 className="w-6 h-6" />, color: 'from-indigo-500 to-indigo-600' });
        statCards.push({ label: 'Trademark Searches', value: typeof res.data.trademarkSearchCount === 'number' ? res.data.trademarkSearchCount : 0, change: '', icon: <BarChart3 className="w-6 h-6" />, color: 'from-blue-500 to-blue-600' });
        if (res.data.analyticsData && res.data.analyticsData.length > 0) {
          const totalAnalyzed = res.data.analyticsData.reduce(
            (acc, cur) => acc + (cur.patents || 0) + (cur.trademarks || 0),
            0
          );

          statCards.push({
            label: 'Total Analyzed',
            value: totalAnalyzed,
            change: '+18%',
            icon: <BarChart3 className="w-6 h-6" />,
            color: 'from-purple-500 to-purple-600'
          });
        }

        if (res.data.subscriptions) {
          statCards.push({
            label: 'Active Monitors',
            value: res.data.subscriptions.filter(
              s => s.status === 'ACTIVE'
            ).length,
          });

        }

        if (res.data.competitorActivity) {
          statCards.push({ label: 'Reports Generated', value: res.data.competitorActivity.length, change: '+45', icon: <FileText className="w-6 h-6" />, color: 'from-primary-500 to-primary-600' });
        }
        let recentFilings = [];
        if (res.data.analyticsData && res.data.analyticsData.length > 0) {
          recentFilings = res.data.analyticsData
            .filter(item => item.recentFilings)
            .flatMap(item => item.recentFilings)
            .sort((a, b) => new Date(b.date) - new Date(a.date))
            .slice(0, 5);
        }
        setDashboardData(prev => ({
          ...prev,
          ...res.data,
          statCards,
          recentFilings
        }));

        console.log("Landscape Pie:", res.data.techPieData);

      } catch (err) {
        console.error('Error fetching analyst dashboard data:', err);
      }
    };

    fetchDashboard();

    // Listen for custom event to refresh dashboard after search
    const refreshHandler = () => {
      fetchDashboard();
    };
    window.addEventListener('analyst-dashboard-refresh', refreshHandler);
    return () => {
      window.removeEventListener('analyst-dashboard-refresh', refreshHandler);
    };
  }, [filters]);



  // ===============================
  // NEW: Fetch Subscriptions (Milestone 3)
  // ===============================
  useEffect(() => {
    const fetchSubscriptions = async () => {
      try {
        const res = await api.get('/api/tracker/subscriptionsbyid');
        setDashboardData(prev => ({
          ...prev,
          subscriptions: res.data
        }));
      } catch (err) {
        console.error('Error fetching subscriptions', err);
      }
    };

    if (activeTab === 'subscriptions') {
      fetchSubscriptions();
    }
  }, [activeTab]);




  // ===============================
  // NEW: Fetch Competitors (Milestone 3)
  // ===============================
  useEffect(() => {
    const fetchCompetitors = async () => {
      try {
        const res = await api.get('/api/analyst/competitors');
        setDashboardData(prev => ({
          ...prev,
          competitorActivity: res.data
        }));
      } catch (err) {
        console.error('Error fetching competitors', err);
      }
    };

    if (activeTab === 'competitors') {
      fetchCompetitors();
    }
  }, [activeTab]);



  // ===============================
  // OPTIONAL: Fetch Landscape Charts
  // ===============================

  useEffect(() => {
    const fetchLandscape = async () => {
      try {
        const res = await api.get('/api/analyst/landscape');
        console.log("Landscape Data Check:", res.data); // Console mein check karein data aa raha hai ya nahi

        setDashboardData(prev => ({
          ...prev,
          analyticsData: res.data.analyticsData || [],
          trendData: res.data.trendData || [],
          techPieData: res.data.techPieData || [],
          citationData: res.data.citationData || [], // Ensure this is not undefined
          familyData: res.data.familyData || []    // Ensure this is not undefined
        }));
      } catch (err) {
        console.error("Landscape fetch error", err);
      }
    };
    fetchLandscape();
  }, []);





  useEffect(() => {
    const fetchLifecycle = async () => {
      const res = await api.get('/api/tracker/analyst/lifecycle');
      setLifecycleStats(res.data);
    };
    fetchLifecycle();
  }, []);











  // Add a search handler that dispatches the refresh event after search
  const handleDashboardSearch = async (searchParams) => {
    try {
      await api.post('/api/search/all', searchParams);
      // After search, trigger dashboard refresh
      window.dispatchEvent(new Event('analyst-dashboard-refresh'));
    } catch (error) {
      console.error('Error searching from dashboard:', error);
    }
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

      // Load from localStorage immediately
      setUser(JSON.parse(userData));
      setLoading(false);

      // Then try to fetch fresh data in background
      try {
        const response = await api.get('/api/profile');
        setUser(response.data);
        localStorage.setItem('user', JSON.stringify(response.data));
      } catch (error) {
        console.error('Error fetching profile (using cached data):', error);
        // Keep using localStorage data, don't redirect
      }
    };

    fetchProfile();
  }, [navigate]);

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };



  const handleUnsubscribe = async (extId) => {
    if (!window.confirm("Are you sure you want to stop monitoring this asset?")) return;

    try {
      await api.post('/api/tracker/unsubscribe', { externalId: extId });

      setDashboardData(prev => ({
        ...prev,
        subscriptions: prev.subscriptions.filter(
          s => s.ipAsset?.externalId !== extId
        )
      }));
    } catch (err) {
      console.error("Unsubscribe error", err);
      alert("Failed to unsubscribe asset");
    }
  };

  const loadFilingHistory = async (assetId) => {
    try {

      const res = await api.get(`/api/tracker/filings/${assetId}`);
      setFilingHistory(res.data);
    } catch (err) {
      console.error("Filing load error", err);
    }
  };



  // Toggle subscription status in dashboardData
  const toggleSubscription = async (subscriptionId) => {
    await api.post('/api/tracker/toggle', { subscriptionId });

    const subs = await api.get('/api/tracker/subscriptionsbyid');
    const landscape = await api.get('/api/analyst/landscape');
    const lifecycle = await api.get('/api/tracker/analyst/lifecycle');

    setDashboardData(prev => ({
      ...prev,
      subscriptions: subs.data,
      analyticsData: landscape.data.analyticsData,
      trendData: landscape.data.trendData,
      techPieData: landscape.data.techPieData,
      citationData: landscape.data.citationData,
      familyData: landscape.data.familyData
    }));

    setLifecycleStats(lifecycle.data);




  };


  // ===============================
  // SAFE DATA FOR LANDSCAPE CHARTS
  // ===============================

  const safeCitationData = Array.isArray(dashboardData.citationData)
    ? dashboardData.citationData.map(d => ({
      application: d.application || d.title || 'Unknown',
      citations: d.citations ?? d.count ?? 0
    }))
    : [];

  const safeTechPieData = Array.isArray(dashboardData.techPieData)
    ? dashboardData.techPieData
      .filter(d => d && Number(d.value) > 0)
      .map(d => ({
        name: String(d.name),
        value: Number(d.value)
      }))
    : [];

  const safeFamilyData = Array.isArray(dashboardData.familyData)
    ? dashboardData.familyData.map(d => ({
      family: d.family || d.title || 'Family',
      size: d.size ?? d.count ?? 0
    }))
    : [];




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
    <>
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 transition-colors">
        {/* Sidebar */}
        <aside className={`fixed left-0 top-0 h-full bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 shadow-lg transition-all duration-300 z-40 ${sidebarOpen ? 'w-64' : 'w-20'}`}>
          <div className="flex flex-col h-full">
            <div className="p-6 border-b border-gray-200 dark:border-gray-700">
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 bg-primary-600 rounded-xl flex items-center justify-center">
                  <VscGraphLine className="w-6 h-6 text-white" />
                </div>
                {sidebarOpen && (
                  <div>
                    <h1 className="text-xl font-light text-gray-900 dark:text-white">GlobalIP</h1>
                    <p className="text-xs text-gray-600 dark:text-gray-400 font-light">Analyst Dashboard</p>
                  </div>
                )}
              </div>
            </div>

            <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
              {navButtons.map((item) => (
                <button
                  key={item.id}
                  onClick={() => {
                    if (item.isRoute) {
                      navigate('/search');
                    } else {
                      setActiveTab(item.id);
                    }
                  }}
                  className={`w-full flex items-center space-x-3 px-4 py-3 rounded-xl transition-all ${activeTab === item.id ? 'bg-gradient-to-r from-primary-500 to-accent-500 text-white shadow-lg' : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'}`}
                >
                  {/* You can add icon or label here if needed, e.g. */}
                  <span>{item.label}</span>
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
                <h2 className="text-2xl font-light text-gray-900 dark:text-white">Analyst Workspace</h2>
                <p className="text-gray-600 dark:text-gray-400 font-light">Advanced IP intelligence and competitive analysis</p>
                {/* Removed Patent/Trademark Searches badges as requested */}
              </div>
              {/* Right side of header (profile, etc.) remains here if needed */}
            </div>
          </header>

          <main className="p-8">
            {activeTab === 'analytics' && (
              <div className="space-y-8">

                {/* ===== IP LIFECYCLE OVERVIEW (DYNAMIC) ===== */}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">

                  <div className="p-6 bg-white dark:bg-gray-800 rounded-2xl shadow-sm border-b-4 border-yellow-500">
                    <h4 className="text-gray-500 text-sm font-medium">Application</h4>
                    <p className="text-3xl font-bold">
                      {lifecycleStats.APPLICATION}
                    </p>
                  </div>

                  <div className="p-6 bg-white dark:bg-gray-800 rounded-2xl shadow-sm border-b-4 border-green-500">
                    <h4 className="text-gray-500 text-sm font-medium">Granted</h4>
                    <p className="text-3xl font-bold">
                      {lifecycleStats.GRANTED}
                    </p>
                  </div>

                  <div className="p-6 bg-white dark:bg-gray-800 rounded-2xl shadow-sm border-b-4 border-blue-500">
                    <h4 className="text-gray-500 text-sm font-medium">Renewal</h4>
                    <p className="text-3xl font-bold">
                      {lifecycleStats.RENEWAL}
                    </p>
                  </div>

                  <div className="p-6 bg-white dark:bg-gray-800 rounded-2xl shadow-sm border-b-4 border-red-500">
                    <h4 className="text-gray-500 text-sm font-medium">Expired</h4>
                    <p className="text-3xl font-bold">
                      {lifecycleStats.EXPIRED}
                    </p>
                  </div>

                </div>





               
                {/* Graphs and dynamic badges side by side */}
                <div className="flex flex-col lg:flex-row gap-8 mb-8 w-full">
                  <div className={`grid gap-10 flex-1 ${safeTechPieData.length > 0 ? 'lg:grid-cols-3' : 'lg:grid-cols-2'}`}>
                    {/* IP Activity Trends */}
                    <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
                      <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-2">IP Activity Trends</h2>
                      <p className="text-gray-500 dark:text-gray-400 mb-4">Monthly patent and trademark activity</p>
                      <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4 flex items-center space-x-2">
                        <Activity className="w-5 h-5 text-purple-600" />
                        <span>patents</span>
                        <span className="ml-2">trademarks</span>
                      </h3>
                      <ResponsiveContainer width="100%" height={300}>
                        <AreaChart data={dashboardData.analyticsData}>
                          <defs>
                            <linearGradient id="colorPatents" x1="0" y1="0" x2="0" y2="1">
                              <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.8} />
                              <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0} />
                            </linearGradient>
                          </defs>
                          <CartesianGrid strokeDasharray="3 3" stroke={theme === 'dark' ? '#374151' : '#e5e7eb'} />
                          <XAxis dataKey="date" stroke={theme === 'dark' ? '#9ca3af' : '#6b7280'} />
                          <YAxis stroke={theme === 'dark' ? '#9ca3af' : '#6b7280'} />
                          <Tooltip contentStyle={{ backgroundColor: theme === 'dark' ? '#1f2937' : '#fff', border: '1px solid #d1d5db' }} />
                          <Legend />
                          <Area type="monotone" dataKey="patents" stroke="#8b5cf6" fillOpacity={1} fill="url(#colorPatents)" />
                          <Area type="monotone" dataKey="trademarks" stroke="#1677ff" fillOpacity={1} fill="url(#colorPatents)" />
                        </AreaChart>
                      </ResponsiveContainer>
                    </div>
                    {/* Technology Growth */}
                    <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
                      <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-2">Technology Growth</h2>
                      <p className="text-gray-500 dark:text-gray-400 mb-4">Growth by technology area</p>
                      <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4 flex items-center space-x-2">
                        <TrendingUp className="w-5 h-5 text-purple-600" />
                        <span>Growth</span>
                      </h3>
                      <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={dashboardData.trendData}>
                          <CartesianGrid strokeDasharray="3 3" stroke={theme === 'dark' ? '#374151' : '#e5e7eb'} />
                          <XAxis dataKey="technology" stroke={theme === 'dark' ? '#9ca3af' : '#6b7280'} />
                          <YAxis stroke={theme === 'dark' ? '#9ca3af' : '#6b7280'} />
                          <Tooltip contentStyle={{ backgroundColor: theme === 'dark' ? '#1f2937' : '#fff', border: '1px solid #d1d5db' }} />
                          <Bar dataKey="growth" fill="#8b5cf6" />
                        </BarChart>
                      </ResponsiveContainer>
                    </div>
                    {/* Technology Distribution Pie Chart (conditionally rendered as third column) */}
                    {safeTechPieData.length > 0 && (
                      <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
                        <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-2">Technology Distribution</h2>
                        <p className="text-gray-500 dark:text-gray-400 mb-4">Share of filings by technology</p>
                        <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4 flex items-center space-x-2">
                          <PieIcon className="w-5 h-5 text-pink-600" />
                          <span>Distribution</span>
                        </h3>
                        <ResponsiveContainer width="100%" height={300}>
                          <PieChart>
                            <Pie
                              data={safeTechPieData}
                              dataKey="value"
                              nameKey="name"
                              cx="50%"
                              cy="50%"
                              outerRadius={100}
                              label
                            />

                            <Tooltip contentStyle={{ backgroundColor: theme === 'dark' ? '#1f2937' : '#fff', border: '1px solid #d1d5db' }} />
                          </PieChart>
                        </ResponsiveContainer>
                      </div>
                    )}
                  </div>
                  {/* Dynamic badges on the right */}
                  <div className="flex flex-col items-end space-y-6 min-w-[320px] lg:ml-8">
                    <DynamicBadge
                      label="Total Searches"
                      value={dashboardData.totalSearches || 0}
                      gradient="from-blue-500 to-indigo-700"
                    />
                    <DynamicBadge
                      label="Patent Searches"
                      value={dashboardData.patentSearchCount}
                      gradient="from-indigo-500 to-indigo-700"
                    />
                    <DynamicBadge
                      label="Trademark Searches"
                      value={dashboardData.trademarkSearchCount}
                      gradient="from-blue-500 to-blue-700"
                    />
                  </div>
                </div>
                {/* Dynamic stat cards */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                  {dashboardData.statCards && dashboardData.statCards.map((stat, idx) => (
                    <div key={idx} className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6 hover:shadow-xl transition-shadow">
                      <div className="flex items-center justify-between mb-4">
                        <div className={`w-12 h-12 bg-gradient-to-br ${stat.color} rounded-xl flex items-center justify-center text-white shadow-lg`}>
                          {stat.icon}
                        </div>
                        <span className="text-green-600 dark:text-green-400 text-sm font-semibold">{stat.change}</span>
                      </div>
                      <p className="text-gray-600 dark:text-gray-400 text-sm mb-1">{stat.label}</p>
                      <p className="text-3xl font-light text-gray-900 dark:text-white">{stat.value}</p>
                    </div>
                  ))}
                </div>
                {/* New dynamic feature: Recent Filings table */}
                {dashboardData.recentFilings && dashboardData.recentFilings.length > 0 && (
                  <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6 mt-8">
                    <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4 flex items-center space-x-2">
                      <FileText className="w-5 h-5 text-blue-600" />
                      <span>Recent Filings</span>
                    </h3>
                    <div className="overflow-x-auto">
                      <table className="w-full">
                        <thead>
                          <tr className="border-b border-gray-200 dark:border-gray-700">
                            <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Title</th>
                            <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Type</th>
                            <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Jurisdiction</th>
                            <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Date</th>
                          </tr>
                        </thead>
                        <tbody>
                          {dashboardData.recentFilings.map((filing, idx) => (
                            <tr key={idx} className="border-b border-gray-100 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
                              <td className="py-4 px-4 font-medium text-gray-900 dark:text-white">{filing.title}</td>
                              <td className="py-4 px-4 text-gray-600 dark:text-gray-400">{filing.type}</td>
                              <td className="py-4 px-4 text-gray-600 dark:text-gray-400">{filing.jurisdiction}</td>
                              <td className="py-4 px-4 text-gray-600 dark:text-gray-400">{filing.date}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'subscriptions' && (
              <div className="space-y-6">
                <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
                  <div className="flex items-center justify-between mb-6">
                    <h3 className="text-xl font-medium text-gray-900 dark:text-white flex items-center space-x-2">
                      <Bell className="w-6 h-6 text-purple-600" />
                      <span>Active Subscriptions</span>
                    </h3>



                    <button
                      onClick={() => navigate('/search')}
                      className="px-4 py-2 bg-gradient-to-r from-purple-600 to-purple-700 text-white rounded-lg hover:shadow-lg transition-all"
                    >
                      + New Subscription
                    </button>




                  </div>


                  {/* ================= New Subscription Modal ================= */}
                  {showSubscriptionModal && (
                    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
                      <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl w-full max-w-lg p-6">

                        <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
                          Create New Subscription
                        </h3>

                        <div className="space-y-4">
                          <input
                            type="text"
                            placeholder="Keyword / Assignee / Inventor"
                            className="w-full px-4 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                          />

                          <select className="w-full px-4 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600">
                            <option>Patent</option>
                            <option>Trademark</option>
                          </select>

                          <select className="w-full px-4 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600">
                            <option>United States (US)</option>
                            <option>Europe (EP)</option>
                            <option>India (IN)</option>
                            <option>China (CN)</option>
                          </select>
                        </div>

                        <div className="flex justify-end space-x-3 mt-6">
                          <button
                            onClick={() => setShowSubscriptionModal(false)}
                            className="px-4 py-2 bg-gray-200 dark:bg-gray-700 rounded-lg"
                          >
                            Cancel
                          </button>

                          <button
                            onClick={() => setShowSubscriptionModal(false)}
                            className="px-4 py-2 bg-purple-600 text-white rounded-lg"
                          >
                            Save Subscription
                          </button>
                        </div>

                      </div>
                    </div>
                  )}


                  <div className="space-y-4">
                    {dashboardData.subscriptions.map((sub) => (
                      <div key={sub.id} className={`p-6 border-2 rounded-xl transition-all ${sub.status === 'ACTIVE'
                        ? 'border-purple-200 dark:border-purple-800 bg-purple-50 dark:bg-purple-900/20' : 'border-gray-200 dark:border-gray-700'}`}>
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <div className="flex items-center space-x-3 mb-2">
                              <h4 className="font-bold text-gray-900 dark:text-white text-lg">
                                {sub.ipAsset?.title}
                              </h4>

                              <span className={`px-3 py-1 rounded-full text-xs font-semibold ${sub.status === 'ACTIVE'
                                ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400'}`}>
                                {sub.status}
                              </span>
                            </div>
                            <div className="flex items-center space-x-6 text-sm text-gray-600 dark:text-gray-400 mb-3">
                              <div className="flex items-center space-x-1">
                                <Globe className="w-4 h-4" />
                                <span>{sub.ipAsset?.jurisdiction}</span>
                              </div>
                              <div className="flex items-center space-x-1">
                                <FileText className="w-4 h-4" />
                                <span>Status: {sub.ipAsset?.status}</span>
                              </div>
                              <div className="flex items-center space-x-1">
                                <AlertCircle className="w-4 h-4" />
                                <span>
                                  Updated {sub.lastUpdate ? sub.lastUpdate : '—'}
                                </span>

                              </div>
                            </div>
                            <div className="flex flex-wrap items-center gap-3 mt-4">

                              {/* Resume / Pause */}
                              <button
                                onClick={() => toggleSubscription(sub.id)}
                                className={`inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold rounded-lg transition ${sub.status === 'ACTIVE'
                                  ? 'bg-yellow-500/10 text-yellow-600 hover:bg-yellow-500/20'
                                  : 'bg-green-500/10 text-green-600 hover:bg-green-500/20'
                                  }`}
                              >
                                {sub.status === 'ACTIVE'
                                  ? <Pause className="w-4 h-4" />
                                  : <Play className="w-4 h-4" />}
                                {sub.status === 'ACTIVE' ? 'Pause' : 'Resume'}
                              </button>


                              {/* Activity Log */}
                              <button
                                onClick={() => {
                                  setSelectedAssetForFilings(sub.ipAsset);
                                  loadFilingHistory(sub.ipAsset.id);
                                }}
                                className="inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 transition"
                              >
                                <Activity className="w-4 h-4" />
                                Activity Log
                              </button>

                              {/* View */}
                              <button
                                onClick={() => handleViewAsset(sub.ipAsset)}
                                className="inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold rounded-lg bg-blue-600 text-white hover:bg-blue-700 transition"
                              >
                                <Eye className="w-4 h-4" />
                                View
                              </button>


                              <button
                                onClick={() => loadLifecycle(sub.ipAsset.id, sub.ipAsset)}
                                className="inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold rounded-lg bg-blue-600 text-white hover:bg-blue-700"
                              >
                                <Eye className="w-4 h-4" />
                                View Lifecycle
                              </button>


                              {/* Remove */}
                              <button
                                onClick={() => handleUnsubscribe(sub.ipAsset.externalId)}
                                className="inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold rounded-lg bg-red-500/10 text-red-600 hover:bg-red-500/20 transition"
                              >
                                <Trash2 className="w-4 h-4" />
                                Remove
                              </button>

                            </div>

                          </div>
                        </div>
                      </div>
                    ))}
                  </div>

                  <div className="mt-6 p-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-xl">
                    <div className="flex items-start space-x-3">
                      <AlertCircle className="w-5 h-5 text-blue-600 dark:text-blue-400 flex-shrink-0 mt-0.5" />
                      <div>
                        <h4 className="font-semibold text-blue-900 dark:text-blue-100 mb-1">Subscription Monitoring</h4>
                        <p className="text-sm text-blue-700 dark:text-blue-300">Monitor patent and trademark filings by keyword, assignee, or inventor across multiple jurisdictions. Receive real-time alerts for new filings and legal status changes.</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'competitors' && (
              <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-100 dark:border-gray-700 p-6">
                <h3 className="text-xl font-medium text-gray-900 dark:text-white mb-6 flex items-center space-x-2">
                  <Target className="w-6 h-6 text-purple-600" />
                  <span>Competitor Analysis</span>
                </h3>
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr className="border-b border-gray-200 dark:border-gray-700">
                        <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Company</th>
                        <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Filings</th>
                        <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Granted</th>
                        <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Pending</th>
                        <th className="text-left py-3 px-4 text-gray-900 dark:text-white font-semibold">Trend</th>
                      </tr>
                    </thead>
                    <tbody>
                      {dashboardData.competitorActivity.map((comp, idx) => (
                        <tr key={idx} className="border-b border-gray-100 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
                          <td className="py-4 px-4 font-medium text-gray-900 dark:text-white">{comp.company}</td>
                          <td className="py-4 px-4 text-gray-600 dark:text-gray-400">{comp.filings}</td>
                          <td className="py-4 px-4 text-gray-600 dark:text-gray-400">{comp.grants}</td>
                          <td className="py-4 px-4 text-gray-600 dark:text-gray-400">{comp.pending}</td>
                          <td className="py-4 px-4">
                            <span className="px-3 py-1 bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300 rounded-full text-sm font-semibold">
                              {comp.trend}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}


            {selectedAssetForFilings && (
              <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50">
                <div className="bg-white dark:bg-gray-800 rounded-3xl w-full max-w-2xl p-8 shadow-2xl mx-4">
                  <div className="flex justify-between items-center mb-6">
                    <h3 className="text-xl font-bold">
                      Activity Log: {selectedAssetForFilings.title}
                    </h3>
                    <button onClick={() => setSelectedAssetForFilings(null)}>
                      <X />
                    </button>
                  </div>

                  <div className="max-h-[400px] overflow-y-auto text-gray-600 dark:text-gray-300">
                    {/* Backend se filings map honge */}
                    <div className="mt-4 overflow-x-auto">
                      <table className="w-full text-left">
                        <thead className="border-b dark:border-gray-700 text-gray-400 uppercase text-xs">
                          <tr>
                            <th className="py-3 px-2">Date</th>
                            <th className="py-3 px-2">Event</th>
                            <th className="py-3 px-2">Description</th>
                          </tr>
                        </thead>
                        <tbody>
                          {filingHistory.length > 0 ? (
                            filingHistory.map(f => (
                              <tr key={f.id} className="border-b dark:border-gray-700 last:border-0">
                                <td className="py-3 px-2">
                                  {new Date(f.date).toLocaleDateString()}
                                </td>
                                <td className="py-3 px-2 font-semibold text-indigo-600">
                                  {f.status}
                                </td>
                                <td className="py-3 px-2 text-sm">
                                  {f.description}
                                </td>
                              </tr>
                            ))
                          ) : (
                            <tr>
                              <td colSpan="3" className="py-6 text-center text-gray-400">
                                No filing history available
                              </td>
                            </tr>
                          )}
                        </tbody>
                      </table>
                    </div>

                  </div>
                </div>
              </div>
            )}



            {/* ================= TRENDS TAB ================= */}
            {activeTab === 'trends' && (
              <div className="space-y-8 animate-in fade-in duration-500">
                {/* Header Section */}
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <div>
                    <h3 className="text-2xl font-bold text-gray-900 dark:text-white">
                      IP Landscape Trends
                    </h3>
                    <p className="text-gray-500 dark:text-gray-400">
                      Visualization of global filing trajectories and technology lifecycles.
                    </p>
                  </div>
                  <div className="flex items-center space-x-3">
                    <button className="flex items-center space-x-2 px-4 py-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl shadow-sm hover:bg-gray-50 transition-colors">
                      <Globe className="w-4 h-4 text-primary-600" />
                      <span>Global View</span>
                    </button>
                  </div>
                </div>

                {/* Chart Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">

                  {/* Technology Growth */}
                  <div className="bg-white dark:bg-gray-800 p-6 rounded-2xl shadow-lg border">
                    <h4 className="font-semibold text-lg flex items-center gap-2 mb-4">
                      <TrendingUp className="w-5 h-5 text-green-500" />
                      Technology Growth Trends
                    </h4>

                    <ResponsiveContainer width="100%" height={350}>
                      <LineChart data={dashboardData.trendData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="technology" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Line
                          type="monotone"
                          dataKey="growth"
                          stroke="#8b5cf6"
                          strokeWidth={3}
                        />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                </div>

                {/* Citation & Family Insight */}
                <div className="bg-gradient-to-br from-indigo-600 to-purple-700 rounded-2xl p-8 text-white">
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="text-xl font-bold">
                        Landscape Visualization: Citation Impact & Patent Families
                      </h4>
                      <p className="text-indigo-100">
                        Analysis of patent influence through citations and global protection
                        strength using patent family coverage.
                      </p>

                    </div>
                    <button
                      onClick={() => setShowReportModal(true)}
                      className="px-6 py-3 bg-white text-indigo-600 rounded-xl font-bold"
                    >
                      Generate Detailed Report
                    </button>

                  </div>
                </div>
              </div>
            )}

            {showReportModal && (
              <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center">
                <div
                  id="report-content"
                  className="bg-white dark:bg-gray-900 w-full max-w-5xl rounded-3xl shadow-2xl p-8 overflow-y-auto max-h-[90vh]"
                >


                  {/* Header */}
                  <div className="flex justify-between items-center mb-6">
                    <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
                      📊 IP Intelligence Detailed Report
                    </h2>
                    <button
                      onClick={() => setShowReportModal(false)}
                      className="text-gray-500 hover:text-red-500"
                    >
                      <X className="w-6 h-6" />
                    </button>
                  </div>

                  {/* ===== Summary Section ===== */}
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                    <div className="p-5 rounded-xl bg-indigo-50 dark:bg-indigo-900/30">
                      <h4 className="text-sm text-gray-500">Total Searches</h4>
                      <p className="text-3xl font-bold">{dashboardData.totalSearches || 0}</p>
                    </div>
                    <div className="p-5 rounded-xl bg-green-50 dark:bg-green-900/30">
                      <h4 className="text-sm text-gray-500">Active Subscriptions</h4>
                      <p className="text-3xl font-bold">
                        {dashboardData.subscriptions?.filter(
                          s => s.status === 'ACTIVE'
                        ).length}

                      </p>
                    </div>
                    <div className="p-5 rounded-xl bg-pink-50 dark:bg-pink-900/30">
                      <h4 className="text-sm text-gray-500">Tracked Technologies</h4>
                      <p className="text-3xl font-bold">
                        {dashboardData.techPieData?.length || 0}
                      </p>
                    </div>
                  </div>

                  {/* ===== Technology Distribution ===== */}
                  <div className="mb-10">
                    <h3 className="text-lg font-semibold mb-4">Technology Distribution</h3>
                    <ResponsiveContainer width="100%" height={300}>
                      <PieChart>
                        <Pie
                          data={safeTechPieData}
                          dataKey="value"
                          nameKey="name"
                          cx="50%"
                          cy="50%"
                          outerRadius={110}
                          label
                        />

                        <Tooltip />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>

                  {/* ===== Filing Trend ===== */}
                  <div className="mb-10">
                    <h3 className="text-lg font-semibold mb-4">Filing Trends</h3>
                    <ResponsiveContainer width="100%" height={300}>
                      <AreaChart data={dashboardData.analyticsData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="date" />
                        <YAxis />
                        <Tooltip />
                        <Area type="monotone" dataKey="patents" stroke="#6366f1" fill="#c7d2fe" />
                        <Area type="monotone" dataKey="trademarks" stroke="#0ea5e9" fill="#bae6fd" />
                      </AreaChart>
                    </ResponsiveContainer>
                  </div>

                  {/* ===== Competitor Summary ===== */}
                  <div className="mb-10">
                    <h3 className="text-lg font-semibold mb-4">Competitor Overview</h3>
                    <table className="w-full text-sm border">
                      <thead className="bg-gray-100 dark:bg-gray-800">
                        <tr>
                          <th className="p-3 text-left">Company</th>
                          <th className="p-3">Filings</th>
                          <th className="p-3">Granted</th>
                          <th className="p-3">Pending</th>
                        </tr>
                      </thead>
                      <tbody>
                        {dashboardData.competitorActivity?.map((c, i) => (
                          <tr key={i} className="border-t">
                            <td className="p-3 font-semibold">{c.company}</td>
                            <td className="p-3 text-center">{c.filings}</td>
                            <td className="p-3 text-center">{c.grants}</td>
                            <td className="p-3 text-center">{c.pending}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {/* Footer */}
                  <div className="flex justify-end gap-3">
                    <button
                      onClick={() => setShowReportModal(false)}
                      className="px-6 py-2 rounded-lg bg-gray-200 dark:bg-gray-700"
                    >
                      Close
                    </button>
                    <button
                      onClick={exportCSV}
                      className="px-6 py-2 rounded-lg bg-green-600 text-white"
                    >
                      Export CSV
                    </button>

                    <button
                      onClick={exportPDF}
                      className="px-6 py-2 rounded-lg bg-indigo-600 text-white"
                    >
                      Export PDF
                    </button>

                  </div>

                </div>
              </div>
            )}


            {/* ===== Summary Section ===== */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
              ...
            </div>


            {activeTab !== 'trends' && (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-10">

                {/* ===== Citation Impact ===== */}
                <div className="bg-white dark:bg-gray-800 p-6 rounded-2xl shadow-lg border">
                  <div className="flex items-center justify-between mb-4">
                    <h4 className="text-lg font-bold">📚 Citation Impact</h4>
                    <span className="text-xs px-3 py-1 bg-indigo-100 text-indigo-700 rounded-full">
                      Forward Citations
                    </span>
                  </div>

                  <p className="text-sm text-gray-500 mb-4">
                    Indicates technological influence based on how often patents are cited by later filings.
                  </p>

                  <ResponsiveContainer width="100%" height={280}>
                    <BarChart data={safeCitationData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="application" hide />
                      <YAxis />
                      <Tooltip />
                      <Bar dataKey="citations" radius={[6, 6, 0, 0]} fill="#6366f1" />
                    </BarChart>
                  </ResponsiveContainer>

                  <p className="mt-3 text-xs text-gray-400">
                    🔍 Higher citations = higher IP impact
                  </p>
                </div>

                {/* ===== Patent Family Strength ===== */}
                <div className="bg-white dark:bg-gray-800 p-6 rounded-2xl shadow-lg border">
                  <div className="flex items-center justify-between mb-4">
                    <h4 className="text-lg font-bold">🌍 Patent Family Strength</h4>
                    <span className="text-xs px-3 py-1 bg-green-100 text-green-700 rounded-full">
                      Global Coverage
                    </span>
                  </div>

                  <p className="text-sm text-gray-500 mb-4">
                    Shows how widely a patent is protected across different countries.
                  </p>

                  <ResponsiveContainer width="100%" height={280}>
                    <BarChart data={safeFamilyData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="family" hide />
                      <YAxis />
                      <Tooltip />
                      <Bar dataKey="size" radius={[6, 6, 0, 0]} fill="#22c55e" />
                    </BarChart>
                  </ResponsiveContainer>

                  <p className="mt-3 text-xs text-gray-400">
                    🌐 Larger family = stronger global protection
                  </p>
                </div>

              </div>

            )}


            {/* ===== Technology Distribution ===== */}







            {viewAsset && (
              <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50">
                <div className="bg-white dark:bg-gray-800 rounded-3xl w-full max-w-xl p-6 shadow-2xl">

                  <div className="flex justify-between items-center mb-4">
                    <h3 className="text-xl font-bold text-gray-900 dark:text-white">
                      Asset Details
                    </h3>
                    <button onClick={() => setViewAsset(null)}>
                      <X />
                    </button>
                  </div>

                  <div className="space-y-3 text-sm text-gray-700 dark:text-gray-300">
                    <p><strong>Title:</strong> {viewAsset.title}</p>
                    <p><strong>Type:</strong> {viewAsset.type}</p>
                    <p><strong>Jurisdiction:</strong> {viewAsset.jurisdiction}</p>
                    <p><strong>Status:</strong> {viewAsset.status}</p>
                    <p><strong>Application No:</strong> {viewAsset.applicationNumber}</p>
                    <p><strong>Filing Date:</strong> {viewAsset.filingDate}</p>
                    <p><strong>Assignee:</strong> {viewAsset.assignee}</p>
                  </div>

                  <div className="flex justify-end mt-6">
                    <button
                      onClick={() => setViewAsset(null)}
                      className="px-4 py-2 bg-indigo-600 text-white rounded-lg"
                    >
                      Close
                    </button>
                  </div>

                </div>
              </div>
            )}


            {selectedLifecycle.length > 0 && selectedLifecycleAsset && (
              <div className="bg-white dark:bg-gray-800 p-6 rounded-2xl shadow-lg mt-6">
                <h4 className="text-lg font-semibold mb-4">
                  IP Lifecycle – {selectedLifecycleAsset.title}
                </h4>

                <ul className="space-y-3">
                  {LIFECYCLE_ORDER.map((stage, i) => {
                    const found = selectedLifecycle.find(l => l.stage === stage);

                    return (
                      <li key={i} className="flex justify-between border-b pb-2">
                        <span className="font-semibold">{stage}</span>
                        <span className="text-gray-500">
                          {found?.date
                            ? new Date(found.date).toLocaleDateString()
                            : 'Not yet'}
                        </span>
                      </li>
                    );
                  })}
                </ul>
              </div>
            )}

          </main>
        </div>
      </div>
    </>
  );
}