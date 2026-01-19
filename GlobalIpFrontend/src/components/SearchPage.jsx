import React, { useState, useEffect } from 'react';
import Toast from './Toast';
import { useNavigate } from 'react-router-dom';
import { Search, Filter, Calendar, MapPin, Building2, ChevronLeft, ChevronRight, Loader2, FileText, X, Eye, ExternalLink, User } from 'lucide-react';
import DarkModeToggle from './DarkModeToggle';
import api from '../api/axios';
import { useTheme } from '../context/ThemeContext';


// Beautiful Filters Button (Reusable)
function FiltersButton({ active, onClick }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`px-6 py-3 rounded-xl font-semibold transition-all flex items-center justify-center space-x-2 shadow-lg border-2 border-primary-500 focus:ring-2 focus:ring-primary-400/40 hover:scale-105 duration-150 ${active ? 'bg-primary-600 text-white' : 'bg-gradient-to-r from-primary-50 to-primary-100 dark:from-gray-700 dark:to-gray-800 text-primary-700 dark:text-white'
        }`}
      style={{ boxShadow: active ? '0 4px 24px 0 rgba(80,80,200,0.10)' : undefined }}
    >
      <Filter className="w-5 h-5" />
      <span>Filters</span>
      {active && <X className="w-4 h-4" />}
    </button>
  );
}


export default function SearchPage() {
  const [toast, setToast] = useState(null);
  const navigate = useNavigate();
  const { theme } = useTheme();
  const userRole = localStorage.getItem('role');
  const [loading, setLoading] = useState(false);
  const [fetchingDetail, setFetchingDetail] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [detailData, setDetailData] = useState(null);
  const [searchResults, setSearchResults] = useState(() => {
    const saved = sessionStorage.getItem('searchResults');
    return saved ? JSON.parse(saved) : [];
  });
  const [totalResults, setTotalResults] = useState(() => {
    const saved = sessionStorage.getItem('totalResults');
    return saved ? JSON.parse(saved) : 0;
  });
  const [currentPage, setCurrentPage] = useState(() => {
    const saved = sessionStorage.getItem('currentPage');
    return saved ? JSON.parse(saved) : 0;
  });
  const [pageSize, setPageSize] = useState(() => {
    const saved = sessionStorage.getItem('pageSize');
    return saved ? JSON.parse(saved) : 10;
  });
  const [totalPages, setTotalPages] = useState(() => {
    const saved = sessionStorage.getItem('totalPages');
    return saved ? JSON.parse(saved) : 0;
  });
  const [hasNext, setHasNext] = useState(() => {
    const saved = sessionStorage.getItem('hasNext');
    return saved ? JSON.parse(saved) : false;
  });
  const [hasPrevious, setHasPrevious] = useState(() => {
    const saved = sessionStorage.getItem('hasPrevious');
    return saved ? JSON.parse(saved) : false;
  });
  const [searchMode, setSearchMode] = useState('PATENT');
  const [filters, setFilters] = useState(() => {
    const saved = sessionStorage.getItem('filters');
    return saved
      ? JSON.parse(saved)
      : {
        searchValue: '',
        fromDate: '',
        toDate: '',
        jurisdiction: '',
        sortBy: 'publicationDate',
        sortDirection: 'DESC',
      };
  });
  const [subscribingId, setSubscribingId] = useState(null);
  const [searchType, setSearchType] = useState('keyword');
  const [showFilters, setShowFilters] = useState(false);
  const [subscribedAssets, setSubscribedAssets] = useState(new Set());
  const handleSearch = async (page = 0) => {
    setLoading(true);
    try {
      const formatDate = (dateStr) => {
        if (!dateStr) return null;
        if (/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) return dateStr;
        const parts = dateStr.split(/[\/-]/);
        if (parts.length === 3) {
          if (parts[0].length === 4) return dateStr;
          if (parts[2].length === 4) return `${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}`;
        }
        return dateStr;
      };
      const searchRequest = {
        assetType: searchMode === 'TRADEMARK' ? 'TRADEMARK' : 'PATENT',
        dataSource: searchMode === 'TRADEMARK' ? 'TMVIEW' : null,
        fromDate: formatDate(filters.fromDate) || null,
        toDate: formatDate(filters.toDate) || null,
        jurisdiction: filters.jurisdiction || null,
        page: page,
        size: pageSize,
        sortBy: filters.sortBy,
        sortDirection: filters.sortDirection,
        searchType: searchType,
        query: searchType === 'keyword' ? filters.searchValue : null,
        inventor: searchType === 'inventor' ? filters.searchValue : null,
        assignee: searchType === 'assignee' ? filters.searchValue : null
      };
      const response = await api.post('/api/search/all', searchRequest);
      setSearchResults(response.data.assets || []);
      setTotalResults(response.data.totalElements || 0);
      setCurrentPage(response.data.currentPage || 0);
      setTotalPages(response.data.totalPages || 0);
      setHasNext(response.data.hasNext || false);
      setHasPrevious(response.data.hasPrevious || false);
      sessionStorage.setItem('searchResults', JSON.stringify(response.data.assets || []));
      sessionStorage.setItem('totalResults', JSON.stringify(response.data.totalElements || 0));
      sessionStorage.setItem('currentPage', JSON.stringify(response.data.currentPage || 0));
      sessionStorage.setItem('pageSize', JSON.stringify(pageSize));
      sessionStorage.setItem('totalPages', JSON.stringify(response.data.totalPages || 0));
      sessionStorage.setItem('hasNext', JSON.stringify(response.data.hasNext || false));
      sessionStorage.setItem('hasPrevious', JSON.stringify(response.data.hasPrevious || false));
      sessionStorage.setItem('filters', JSON.stringify(filters));
      // Dispatch custom event to trigger dashboard refresh
      window.dispatchEvent(new Event('analyst-dashboard-refresh'));
      // Removed TMVIEW-SELENIUM toast popup
    } catch (error) {
      setSearchResults([]);
      setTotalResults(0);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    await handleSearch(0);
  };

  const handleClearFilters = () => {
    setFilters({
      searchValue: '',
      fromDate: '',
      toDate: '',
      jurisdiction: '',
      sortBy: 'publicationDate',
      sortDirection: 'DESC',
    });
    setSearchResults([]);
    setTotalResults(0);
    sessionStorage.clear();
  };

  const handleViewDetails = async (asset) => {
    if (!asset) return;
    setFetchingDetail(true);
    setDetailModalOpen(true);
    try {
      const idToFetch = asset.publicationNumber || asset.externalId;
      const source = asset.patentOffice || 'EPO';
      const response = await api.get(`/api/search/patent/${idToFetch}`, {
        params: { source }
      });
      setDetailData({ ...response.data, _apiSource: `/api/search/patent/${idToFetch} (source: ${source})` });
    } catch (error) {
      setDetailData(asset);
    } finally {
      setFetchingDetail(false);
    }
  };


  // ================= Subscribe Handler =================
  const handleSubscribe = async (asset) => {

    try {
      setSubscribingId(asset.externalId || asset.id);

      await api.post('/api/tracker/subscribe', {
        ...asset
      });

      const key = asset.externalId || asset.id;
      setSubscribedAssets(prev => new Set(prev).add(key));

      setToast({
        type: 'success',
        message: 'Asset subscribed successfully'
      });

    } catch (err) {
      setToast({
        type: 'error',
        message: 'Failed to subscribe asset'
      });
    } finally {
      setSubscribingId(null);
    }
  };

  // ================= Unsubscribe Handler =================
  const handleUnsubscribe = async (asset) => {
    try {
      const key = asset.externalId || asset.id;

      setSubscribingId(key);

      await api.post('/api/tracker/unsubscribe', {
        externalId: asset.externalId
      });

      setSubscribedAssets(prev => {
        const updated = new Set(prev);
        updated.delete(key);
        return updated;
      });

      setToast({
        type: 'success',
        message: 'Asset unsubscribed successfully'
      });

    } catch (err) {
      setToast({
        type: 'error',
        message: 'Failed to unsubscribe asset'
      });
    } finally {
      setSubscribingId(null);
    }
  };





  const handlePageChange = (page) => {
    if (page < 0 || page >= totalPages) return;
    setCurrentPage(page);
    handleSearch(page);
  };

  // Persist search state in sessionStorage for back navigation
  return (
    <>
      {toast && (
        <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} duration={4000} />
      )}
      <div className="min-h-screen bg-white dark:bg-gray-900 transition-colors relative">
        {/* Modal for View Details */}
        {detailModalOpen && (
          <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/40" style={{ pointerEvents: 'auto' }}>
            <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl border-2 border-primary-500 max-w-2xl w-full mx-4 p-8 relative animate-fadeIn z-[101]" style={{ pointerEvents: 'auto' }}>
              <button
                className="absolute top-4 right-4 text-gray-500 hover:text-primary-600 dark:hover:text-primary-400 transition"
                onClick={() => { setDetailModalOpen(false); setDetailData(null); }}
              >
                <X className="w-6 h-6" />
              </button>
              {fetchingDetail ? (
                <div className="flex flex-col items-center justify-center py-12">
                  <Loader2 className="w-12 h-12 animate-spin text-primary-600 mb-4" />
                  <p className="text-xl font-light text-gray-900 dark:text-white">Loading Deep Details...</p>
                  <p className="text-sm text-gray-500 dark:text-gray-400">Fetching abstract and full title from the data source.</p>
                </div>
              ) : detailData ? (
                <div className="space-y-4">
                  <h2 className="text-2xl font-bold text-primary-700 dark:text-primary-300 mb-2">{detailData.title || 'Untitled IP Asset'}</h2>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <span className="font-semibold text-gray-700 dark:text-gray-300">Type:</span> {detailData.assetType}
                    </div>
                    <div>
                      <span className="font-semibold text-gray-700 dark:text-gray-300">Number:</span> {detailData.publicationNumber || detailData.applicationNumber || '-'}
                    </div>
                    <div>
                      <span className="font-semibold text-gray-700 dark:text-gray-300">Assignee:</span> {detailData.assignee || '-'}
                    </div>
                    <div>
                      <span className="font-semibold text-gray-700 dark:text-gray-300">
                        
                        :</span> {detailData.inventor || '-'}
                    </div>
                    <div>
                      <span className="font-semibold text-gray-700 dark:text-gray-300">Jurisdiction:</span> {detailData.jurisdiction || detailData.patentOffice || '-'}
                    </div>
                    <div>
                      <span className="font-semibold text-gray-700 dark:text-gray-300">Publication Date:</span> {detailData.publicationDate || '-'}
                    </div>
                  </div>
                  {detailData.description && (
                    <div className="mt-4">
                      <span className="font-semibold text-gray-700 dark:text-gray-300">Abstract:</span>
                      <p className="text-gray-800 dark:text-gray-200 mt-1 whitespace-pre-line">{detailData.description}</p>
                    </div>
                  )}
                  {detailData.abstract && (
                    <div className="mt-4">
                      <span className="font-semibold text-gray-700 dark:text-gray-300">Abstract:</span>
                      <p className="text-gray-800 dark:text-gray-200 mt-1 whitespace-pre-line">{detailData.abstract}</p>
                    </div>
                  )}
                  {detailData.claims && (
                    <div className="mt-4">
                      <span className="font-semibold text-gray-700 dark:text-gray-300">Claims:</span>
                      <p className="text-gray-800 dark:text-gray-200 mt-1 whitespace-pre-line text-sm">{detailData.claims}</p>
                    </div>
                  )}
                  <div className="mt-6 flex flex-col gap-2">
                    {detailData.url || detailData.link ? (
                      <div>
                        <span className="font-semibold text-gray-700 dark:text-gray-300">Full Record URL:</span>
                        <a
                          href={detailData.url || detailData.link}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="ml-2 text-primary-600 dark:text-primary-300 underline break-all"
                        >
                          {detailData.url || detailData.link}
                        </a>
                      </div>
                    ) : (
                      <div>
                        <span className="font-semibold text-gray-700 dark:text-gray-300">Full Record URL:</span>
                        <span className="ml-2 text-gray-400">N/A</span>
                      </div>
                    )}
                    {((detailData.patentOffice || detailData.jurisdiction || '').toUpperCase() === 'OPENALEX') && detailData._apiSource ? (
                      <div className="mt-2 text-xs text-gray-500 dark:text-gray-400">
                        <span className="font-semibold">Source API:</span> {detailData._apiSource}
                      </div>
                    ) :
                      (detailData.patentOffice || detailData.jurisdiction) && (
                        <div className="mt-2 text-xs text-gray-500 dark:text-gray-400">
                          <span className="font-semibold">Source:</span> {detailData.patentOffice || detailData.jurisdiction}
                        </div>
                      )}
                  </div>
                </div>
              ) : null}
            </div>
          </div>
        )}
        {/* Header */}
        <div className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 sticky top-0 z-20">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
            <div className="flex items-center justify-between gap-4">
              <button
                onClick={() => navigate(-1)}
                className="flex items-center space-x-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors font-light px-3 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700"
              >
                <ChevronLeft className="w-5 h-5" />
                <span>Back</span>
              </button>
              <h1 className="text-4xl md:text-5xl font-light text-gray-900 dark:text-white tracking-tight">IP Search</h1>
              <DarkModeToggle />
            </div>
          </div>
        </div>


        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Search Form */}
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-8 mb-8">
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="mb-6">
                <h2 className="text-2xl md:text-3xl font-light text-gray-900 dark:text-white mb-2">
                  {searchMode === 'TRADEMARK' ? 'Trademarks' : 'Patents'}
                </h2>
                <p className="text-gray-600 dark:text-gray-400 text-base font-light mb-4">
                  Search by keyword, inventor, or assignee
                </p>
                <div className="flex flex-col lg:flex-row gap-4">
                  {/* Search type buttons */}
                  <div className="flex gap-2 mb-2">
                    <button type="button" className={`px-4 py-2 rounded-lg font-semibold border-2 transition-all ${searchType === 'keyword' ? 'bg-primary-600 text-white border-primary-600' : 'bg-white dark:bg-gray-700 text-primary-700 border-primary-300 dark:border-gray-600'}`} onClick={() => setSearchType('keyword')}>Keyword</button>
                    
                  </div>
                  {searchMode === 'TRADEMARK' && searchType === 'inventor' && (
                    <div className="text-sm text-red-600 dark:text-red-400 mt-2">
                      Inventor filter is not supported for trademark search. Please use keyword or assignee.
                    </div>
                  )}
                  <div className="flex-1 relative group">
                    <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 group-focus-within:text-primary-500 w-5 h-5 transition-colors" />
                    <input
                      type="text"
                      value={filters.searchValue}
                      onChange={(e) => setFilters({ ...filters, searchValue: e.target.value })}
                      placeholder={
                        searchType === 'keyword' ? 'Enter keywords' :
                          searchType === 'inventor' ? 'Enter inventor name' :
                            'Enter assignee name'
                      }
                      className="w-full pl-12 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-primary-500/50 focus:border-primary-500 transition-all text-base font-light"
                    />
                  </div>
                  {/* Only show filter button for non-user roles */}
                  {userRole !== 'USER' && (
                    <FiltersButton active={showFilters} onClick={() => setShowFilters(!showFilters)} />
                  )}
                  <button
                    type="submit"
                    disabled={loading}
                    className="px-8 py-3 bg-primary-600 text-white rounded-lg font-bold shadow hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2"
                  >
                    {loading ? (
                      <>
                        <Loader2 className="w-5 h-5 animate-spin" />
                        <span>Searching...</span>
                      </>
                    ) : (
                      <>
                        <Search className="w-5 h-5" />
                        <span>Search</span>
                      </>
                    )}
                  </button>
                </div>
              </div>


              {/* Only show filter options for non-user roles */}
              {showFilters && userRole !== 'USER' && (
                <div className="pt-6 border-t border-gray-200 dark:border-gray-700 space-y-5">
                  {/* Toggle for Patent/Trademark */}
                  <div className="flex gap-4 mb-4">
                    <button
                      type="button"
                      className={`px-6 py-2 rounded-xl font-semibold border-2 transition-all ${searchMode === 'PATENT' ? 'bg-primary-600 text-white border-primary-600' : 'bg-white dark:bg-gray-700 text-primary-700 border-primary-300 dark:border-gray-600'}`}
                      onClick={() => setSearchMode('PATENT')}
                    >
                      Patent Search
                    </button>
                    <button
                      type="button"
                      className={`px-6 py-2 rounded-xl font-semibold border-2 transition-all ${searchMode === 'TRADEMARK' ? 'bg-primary-600 text-white border-primary-600' : 'bg-white dark:bg-gray-700 text-primary-700 border-primary-300 dark:border-gray-600'}`}
                      onClick={() => setSearchMode('TRADEMARK')}
                    >
                      Trademark Search
                    </button>

                    {searchMode !== 'TRADEMARK' && (
                      <button type="button" className={`px-4 py-2 rounded-lg font-semibold border-2 transition-all ${searchType === 'inventor' ? 'bg-primary-600 text-white border-primary-600' : 'bg-white dark:bg-gray-700 text-primary-700 border-primary-300 dark:border-gray-600'}`} onClick={() => setSearchType('inventor')}>Inventor</button>
                    )}
                    <button type="button" className={`px-4 py-2 rounded-lg font-semibold border-2 transition-all ${searchType === 'assignee' ? 'bg-primary-600 text-white border-primary-600' : 'bg-white dark:bg-gray-700 text-primary-700 border-primary-300 dark:border-gray-600'}`} onClick={() => setSearchType('assignee')}>Assignee</button>






                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    {/* Start Date Field */}
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2 flex items-center">
                        <Calendar className="w-4 h-4 mr-1.5" /> Start Date
                      </label>
                      <input
                        type="date"
                        value={filters.fromDate}
                        onChange={e => setFilters({ ...filters, fromDate: e.target.value })}
                        className="w-full px-4 py-2.5 border-2 border-gray-200 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white transition-all focus:ring-2 focus:ring-primary-500/50 focus:border-primary-500"
                      />
                    </div>
                    {/* End Date Field */}
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2 flex items-center">
                        <Calendar className="w-4 h-4 mr-1.5" /> End Date
                      </label>
                      <input
                        type="date"
                        value={filters.toDate}
                        onChange={e => setFilters({ ...filters, toDate: e.target.value })}
                        className="w-full px-4 py-2.5 border-2 border-gray-200 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white transition-all focus:ring-2 focus:ring-primary-500/50 focus:border-primary-500"
                      />
                    </div>
                    {/* Jurisdiction Field */}
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2 flex items-center">
                        <MapPin className="w-4 h-4 mr-1.5" /> Jurisdiction
                      </label>
                      <input
                        type="text"
                        value={filters.jurisdiction}
                        onChange={e => setFilters({ ...filters, jurisdiction: e.target.value })}
                        placeholder="e.g. US, EP, IN, WIPO, etc."
                        className="w-full px-4 py-2.5 border-2 border-gray-200 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white transition-all focus:ring-2 focus:ring-primary-500/50 focus:border-primary-500"
                      />
                    </div>
                  </div>
                  {/* Clear Filters Button */}
                  <div className="flex justify-end pt-4">
                    <button
                      type="button"
                      onClick={handleClearFilters}
                      className="px-6 py-2.5 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg font-semibold hover:bg-gray-300 dark:hover:bg-gray-600 transition-all"
                    >
                      Clear All Filters
                    </button>
                  </div>
                </div>
              )}
            </form>
          </div>


          {/* Results Table */}
          {totalResults > 0 && (
            <div className="bg-white dark:bg-gray-800 rounded-lg px-6 py-4 mb-6 border border-primary-200 dark:border-primary-700 shadow-sm flex items-center justify-between">
              <p className="text-base text-gray-700 dark:text-gray-300 font-light">
                Found <span className="font-semibold text-primary-600">{totalResults.toLocaleString()}</span> results
              </p>
              <select
                value={pageSize}
                onChange={(e) => { setPageSize(Number(e.target.value)); handleSearch(0); }}
                className="px-4 py-2 border-2 border-primary-200 dark:border-primary-700 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white font-light"
              >
                <option value={10}>10 per page</option>
                <option value={25}>25 per page</option>
                <option value={50}>50 per page</option>
              </select>
            </div>
          )}


          {loading ? (
            <div className="flex items-center justify-center py-32">
              <div className="text-center">
                <Loader2 className="w-16 h-16 animate-spin text-primary-600 mx-auto mb-6" />
                <p className="text-lg font-semibold text-gray-700 dark:text-gray-300">Searching IP databases...</p>
              </div>
            </div>
          ) : searchResults.length > 0 ? (
            <div className="mb-8">
              <div className="bg-white dark:bg-gray-800 rounded-xl border border-primary-200 dark:border-primary-700 shadow overflow-x-auto">
                <div className="px-6 py-3 border-b border-gray-200 dark:border-gray-700 flex items-center gap-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  <span className="w-20">Type</span>
                  <span className="w-44">Number</span>
                  <span className="flex-1 min-w-[180px]">Title</span>
                  <span className="w-44">Assignee</span>
                  <span className="w-28 text-center">Action</span>
                </div>
                {searchResults.map((asset, idx) => (
                  <div
                    key={asset.externalId || idx}
                    className="flex items-center gap-4 px-6 py-4 border-b border-gray-100 dark:border-gray-700 text-sm group hover:bg-primary-50 dark:hover:bg-primary-900/10 transition cursor-pointer"
                    onClick={() => handleViewDetails(asset)}
                  >
                    <span className={`w-20 font-bold ${asset.assetType === 'PATENT' ? 'text-blue-600' : 'text-purple-600'}`}>{asset.assetType}</span>
                    <span className="w-44 font-mono text-xs text-primary-700 dark:text-primary-300 truncate">{asset.publicationNumber || asset.applicationNumber || '-'}</span>
                    <span className="flex-1 min-w-[180px] font-light text-gray-900 dark:text-gray-100 line-clamp-1">{asset.title || 'Untitled IP Asset'}</span>
                    <span className="w-44 font-light text-gray-700 dark:text-gray-300 truncate">{asset.assignee || '-'}</span>
                    <span className="w-28 flex justify-center gap-2">

                      {/* 👁 View */}
                      <button
                        type="button"
                        onClick={e => {
                          e.stopPropagation();
                          handleViewDetails(asset);
                        }}
                        className="inline-flex items-center gap-1 px-3 py-2 rounded-lg border border-primary-500 text-primary-700 text-xs font-semibold hover:bg-primary-100"
                      >
                        <Eye className="h-4 w-4" /> View
                      </button>

                      {/* ⭐ Subscribe */}
                      {(() => {
                        const key = asset.externalId || asset.id;
                        const isSubscribed = subscribedAssets.has(key);

                        return (
                          <button
                            type="button"
                            onClick={e => {
                              e.stopPropagation();
                              isSubscribed
                                ? handleUnsubscribe(asset)
                                : handleSubscribe(asset);
                            }}
                            disabled={subscribingId === key}
                            className={`inline-flex items-center gap-1 px-3 py-2 rounded-lg border text-xs font-semibold ${isSubscribed
                              ? 'border-red-500 text-red-700 hover:bg-red-100'
                              : 'border-green-500 text-green-700 hover:bg-green-100'} disabled:opacity-50`} >
                            {subscribingId === key
                              ? 'Processing...'
                              : isSubscribed
                                ? 'Unsubscribe'
                                : 'Subscribe'}
                          </button>
                        );
                      })()}





                    </span>



                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="bg-white dark:bg-gray-800 rounded-xl p-16 text-center border-2 border-gray-200 dark:border-gray-700">
              {filters.searchValue || filters.fromDate || filters.toDate ? (
                <>
                  <h3 className="text-3xl font-light text-gray-900 dark:text-white mb-3">No results found</h3>
                  <p className="text-gray-600 dark:text-gray-400">Try different keywords, inventor, or assignee. If you expected results, check your spelling or try a broader search.</p>
                </>
              ) : (
                <>
                  <h3 className="text-3xl font-light text-gray-900 dark:text-white mb-3">Start Your IP Search</h3>
                  <p className="text-gray-600 dark:text-gray-400">Enter keywords above or use filters to search by assignee, inventor, or jurisdiction.</p>
                </>
              )}
            </div>
          )}


          {/* Enhanced Pagination */}
          {totalPages > 1 && (
            <div className="mt-8 rounded-xl border-2 border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 px-6 py-5 shadow-lg flex flex-col sm:flex-row items-center gap-4 sm:justify-between">
              <div className="text-sm font-semibold text-gray-700 dark:text-gray-300">
                Page <span className="text-lg font-bold text-primary-600">{currentPage + 1}</span> of <span className="text-lg font-bold text-gray-900 dark:text-white">{totalPages}</span>
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={!hasPrevious}
                  className="px-3 py-2 rounded-xl border-2 border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white disabled:opacity-50 font-bold transition-all flex items-center gap-2 hover:bg-gray-50 dark:hover:bg-gray-600"
                >
                  <ChevronLeft className="h-4 w-4" /> Prev
                </button>
                {(() => {
                  const pages = [];
                  const startPage = Math.max(0, currentPage - 2);
                  const endPage = Math.min(totalPages - 1, currentPage + 2);
                  if (startPage > 0) {
                    pages.push(
                      <button key={0} onClick={() => handlePageChange(0)} className={`px-3 py-2 rounded-lg font-bold border-2 transition-all ${currentPage === 0 ? 'bg-primary-600 text-white border-primary-600' : 'bg-white dark:bg-gray-700 text-gray-900 dark:text-white border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-600'}`}>1</button>
                    );
                    if (startPage > 1) pages.push(<span key="start-ellipsis" className="px-2 text-gray-500">...</span>);
                  }
                  for (let i = startPage; i <= endPage; i++) {
                    if (i === 0 || i === totalPages - 1) continue;
                    pages.push(
                      <button key={i} onClick={() => handlePageChange(i)} className={`px-3 py-2 rounded-lg font-bold border-2 transition-all ${currentPage === i ? 'bg-primary-600 text-white border-primary-600' : 'bg-white dark:bg-gray-700 text-gray-900 dark:text-white border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-600'}`}>{i + 1}</button>
                    );
                  }
                  if (endPage < totalPages - 1) {
                    if (endPage < totalPages - 2) pages.push(<span key="end-ellipsis" className="px-2 text-gray-500">...</span>);
                    pages.push(
                      <button key={totalPages - 1} onClick={() => handlePageChange(totalPages - 1)} className={`px-3 py-2 rounded-lg font-bold border-2 transition-all ${currentPage === totalPages - 1 ? 'bg-primary-600 text-white border-primary-600' : 'bg-white dark:bg-gray-700 text-gray-900 dark:text-white border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-600'}`}>{totalPages}</button>
                    );
                  }
                  return pages;
                })()}
                <button
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={!hasNext}
                  className="px-3 py-2 rounded-xl border-2 border-primary-500 bg-primary-600 text-white font-bold transition-all flex items-center gap-2 disabled:opacity-50 hover:bg-primary-700"
                >
                  Next <ChevronRight className="h-4 w-4" />
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
}
