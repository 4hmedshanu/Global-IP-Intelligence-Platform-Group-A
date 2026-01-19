import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import { Calendar, MapPin, Building2, FileText, User, Hash, ExternalLink } from 'lucide-react';
import DarkModeToggle from './DarkModeToggle';

export default function IpAssetQuickView() {
  const location = useLocation();
  const navigate = useNavigate();
  const asset = location.state?.asset;

  if (!asset) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="text-center">
          <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-2">No asset data found</h2>
          <button
            onClick={() => navigate(-1)}
            className="mt-4 px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
          >
            Go Back
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white dark:bg-gray-900">
      <div className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 sticky top-0 z-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <button
              onClick={() => navigate(-1)}
              className="flex items-center space-x-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors font-light px-3 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700"
            >
              <ChevronLeft className="w-5 h-5" />
              <span>Back to Search</span>
            </button>
            <span className="hidden md:inline-block text-xl font-light text-gray-400 mx-4">|</span>
            <h1 className="text-2xl md:text-3xl font-light text-gray-900 dark:text-white tracking-tight">IP Asset Details</h1>
          </div>
          <DarkModeToggle />
        </div>
      </div>
      <div className="max-w-4xl mx-auto px-4 py-10">
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl border border-gray-200 dark:border-gray-700 p-10">
          {/* Top section: badges and numbers */}
          <div className="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div className="flex items-center gap-3 flex-wrap">
              <span className={`px-4 py-2 rounded-full text-sm font-medium ${
                asset.assetType === 'PATENT'
                  ? 'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300'
                  : 'bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300'
              }`}>
                {asset.assetType}
              </span>
              {asset.status && (
                <span className={`px-4 py-2 rounded-full text-xs font-semibold ${
                  ['GRANTED', 'PUBLISHED', 'Published'].includes(asset.status)
                    ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300'
                    : 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-300'
                }`}>
                  {asset.status}
                </span>
              )}
              {asset.patentOffice && (
                <span className="px-3 py-1 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg text-xs font-medium flex items-center gap-1">
                  <ExternalLink className="w-4 h-4" /> {asset.patentOffice}
                </span>
              )}
              {asset.jurisdiction && (
                <span className="px-3 py-1 bg-primary-50 dark:bg-primary-900/30 text-primary-700 dark:text-primary-300 rounded-lg text-xs font-medium flex items-center gap-1">
                  <MapPin className="w-4 h-4" /> {asset.jurisdiction}
                </span>
              )}
            </div>
            <div className="flex flex-wrap gap-2">
              {asset.publicationNumber && (
                <span className="inline-flex items-center gap-2 rounded-lg bg-gradient-to-r from-primary-50 to-primary-100 dark:from-primary-900/20 dark:to-primary-800/20 px-4 py-2 border-2 border-primary-200 dark:border-primary-700 text-xs font-light text-primary-700 dark:text-primary-300">
                  <Hash className="w-4 h-4" />
                  <span>PUB:</span>
                  <span className="font-mono">{asset.publicationNumber}</span>
                </span>
              )}
              {asset.applicationNumber && (
                <span className="inline-flex items-center gap-2 rounded-lg bg-gray-100 dark:bg-gray-700 px-4 py-2 border-2 border-gray-200 dark:border-gray-600 text-xs font-light text-gray-700 dark:text-gray-200">
                  <Hash className="w-4 h-4" />
                  <span>APP:</span>
                  <span className="font-mono">{asset.applicationNumber}</span>
                </span>
              )}
            </div>
          </div>
          {/* Title and description */}
          <h2 className="text-3xl font-light mb-4 text-gray-900 dark:text-white flex items-center gap-2">
            <FileText className="w-7 h-7 text-primary-500 dark:text-primary-400" />
            {asset.title || 'Untitled IP Asset'}
          </h2>
          {asset.description && (
            <p className="text-lg leading-relaxed text-gray-600 dark:text-gray-400 mb-8 font-light border-l-4 border-primary-200 dark:border-primary-700 pl-4">
              {asset.description}
            </p>
          )}
          {/* Details grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
            {asset.assignee && (
              <div className="flex items-center gap-3">
                <Building2 className="w-6 h-6 text-blue-500 dark:text-blue-400" />
                <div>
                  <div className="text-xs font-semibold text-blue-600 dark:text-blue-400 mb-1">Assignee / Owner</div>
                  <div className="text-base font-light text-gray-900 dark:text-gray-100">{asset.assignee}</div>
                </div>
              </div>
            )}
            {asset.inventor && (
              <div className="flex items-center gap-3">
                <User className="w-6 h-6 text-purple-500 dark:text-purple-400" />
                <div>
                  <div className="text-xs font-semibold text-purple-600 dark:text-purple-400 mb-1">Inventor</div>
                  <div className="text-base font-light text-gray-900 dark:text-gray-100">{asset.inventor}</div>
                </div>
              </div>
            )}
            {asset.applicationDate && (
              <div className="flex items-center gap-3">
                <Calendar className="w-6 h-6 text-green-500 dark:text-green-400" />
                <div>
                  <div className="text-xs font-semibold text-green-600 dark:text-green-400 mb-1">Application Date</div>
                  <div className="text-base font-light text-gray-900 dark:text-gray-100">{new Date(asset.applicationDate).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })}</div>
                </div>
              </div>
            )}
            {asset.publicationDate && (
              <div className="flex items-center gap-3">
                <Calendar className="w-6 h-6 text-orange-500 dark:text-orange-400" />
                <div>
                  <div className="text-xs font-semibold text-orange-600 dark:text-orange-400 mb-1">Publication Date</div>
                  <div className="text-base font-light text-gray-900 dark:text-gray-100">{new Date(asset.publicationDate).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })}</div>
                </div>
              </div>
            )}
            {asset.ipcClasses && asset.ipcClasses.length > 0 && (
              <div className="flex items-center gap-3">
                <span className="w-6 h-6 flex items-center justify-center rounded-full bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-200 font-bold">IP</span>
                <div>
                  <div className="text-xs font-semibold text-slate-600 dark:text-slate-400 mb-1">IPC Classes</div>
                  <div className="flex flex-wrap gap-2 mt-1">
                    {asset.ipcClasses.map((cls, idx) => (
                      <span key={idx} className="inline-flex items-center rounded-full bg-slate-100 dark:bg-slate-700 px-3 py-1.5 text-xs font-light text-slate-700 dark:text-slate-200 border border-slate-200 dark:border-slate-600 shadow-sm">{cls}</span>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
          {/* Extra details */}
          <div className="flex flex-col gap-2 text-sm text-gray-700 dark:text-gray-300">
            {asset.jurisdiction && (
              <div className="flex items-center gap-2"><MapPin className="w-4 h-4" /> Jurisdiction: {asset.jurisdiction}</div>
            )}
            {asset.patentOffice && (
              <div className="flex items-center gap-2"><ExternalLink className="w-4 h-4" /> Patent Office: {asset.patentOffice}</div>
            )}
            {asset.status && (
              <div className="flex items-center gap-2"><FileText className="w-4 h-4" /> Status: {asset.status}</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
