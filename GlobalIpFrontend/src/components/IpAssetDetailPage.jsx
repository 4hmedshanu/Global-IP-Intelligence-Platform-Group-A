import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  ChevronLeft, Calendar, MapPin, Building2, User, FileText, 
  Hash, Bookmark, Share2, Download, ExternalLink, Loader2,
  CheckCircle, Clock, AlertCircle
} from 'lucide-react';
import api from '../api/axios';
import { useTheme } from '../context/ThemeContext';

export default function IpAssetDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { theme } = useTheme();
  const [asset, setAsset] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchAssetDetails();
  }, [id]);

  const fetchAssetDetails = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get(`/api/search/asset/${id}`);
      setAsset(response.data);
    } catch (error) {
      console.error('Error fetching asset details:', error);
      setError('Failed to load IP asset details. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const getStatusIcon = (status) => {
    if (!status) return null;
    const statusUpper = status.toUpperCase();
    if (statusUpper.includes('GRANTED') || statusUpper.includes('PUBLISHED')) {
      return <CheckCircle className="w-5 h-5 text-green-500" />;
    } else if (statusUpper.includes('PENDING') || statusUpper.includes('APPLICATION')) {
      return <Clock className="w-5 h-5 text-yellow-500" />;
    }
    return <AlertCircle className="w-5 h-5 text-gray-500" />;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="w-12 h-12 animate-spin text-primary-600 mx-auto mb-4" />
          <p className="text-gray-600 dark:text-gray-400">Loading IP asset details...</p>
        </div>
      </div>
    );
  }

  if (error || !asset) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center max-w-md">
          <AlertCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
          <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-2">
            {error || 'Asset not found'}
          </h2>
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
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* Header */}
      <div className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <button
              onClick={() => navigate(-1)}
              className="flex items-center space-x-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors"
            >
              <ChevronLeft className="w-5 h-5" />
              <span>Back to Search</span>
            </button>
            <div className="flex items-center space-x-3">
              <button className="p-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors">
                <Bookmark className="w-5 h-5" />
              </button>
              <button className="p-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors">
                <Share2 className="w-5 h-5" />
              </button>
              <button className="p-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors">
                <Download className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Title Section */}
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-8 mb-6">
          <div className="flex items-start justify-between mb-4">
            <div className="flex items-center space-x-3">
              <span className={`px-4 py-2 rounded-full text-sm font-medium ${
                asset.assetType === 'PATENT'
                  ? 'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300'
                  : 'bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300'
              }`}>
                {asset.assetType}
              </span>
              {asset.patentOffice && (
                <span className="px-3 py-1 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg text-sm font-medium">
                  {asset.patentOffice}
                </span>
              )}
            </div>
            {asset.status && (
              <div className="flex items-center space-x-2 px-4 py-2 bg-gray-50 dark:bg-gray-700 rounded-lg">
                {getStatusIcon(asset.status)}
                <span className="text-sm font-medium text-gray-900 dark:text-white">
                  {asset.status}
                </span>
              </div>
            )}
          </div>

          <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-4">
            {asset.title}
          </h1>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {asset.publicationNumber && (
              <div className="flex items-center space-x-2 text-sm">
                <Hash className="w-4 h-4 text-gray-400" />
                <div>
                  <p className="text-gray-500 dark:text-gray-400">Publication No.</p>
                  <p className="font-medium text-gray-900 dark:text-white">{asset.publicationNumber}</p>
                </div>
              </div>
            )}
            {asset.applicationNumber && (
              <div className="flex items-center space-x-2 text-sm">
                <Hash className="w-4 h-4 text-gray-400" />
                <div>
                  <p className="text-gray-500 dark:text-gray-400">Application No.</p>
                  <p className="font-medium text-gray-900 dark:text-white">{asset.applicationNumber}</p>
                </div>
              </div>
            )}
            {asset.applicationDate && (
              <div className="flex items-center space-x-2 text-sm">
                <Calendar className="w-4 h-4 text-gray-400" />
                <div>
                  <p className="text-gray-500 dark:text-gray-400">Application Date</p>
                  <p className="font-medium text-gray-900 dark:text-white">
                    {new Date(asset.applicationDate).toLocaleDateString()}
                  </p>
                </div>
              </div>
            )}
            {asset.publicationDate && (
              <div className="flex items-center space-x-2 text-sm">
                <Calendar className="w-4 h-4 text-gray-400" />
                <div>
                  <p className="text-gray-500 dark:text-gray-400">Publication Date</p>
                  <p className="font-medium text-gray-900 dark:text-white">
                    {new Date(asset.publicationDate).toLocaleDateString()}
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="grid lg:grid-cols-3 gap-6">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* Abstract/Description */}
            {asset.description && (
              <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
                <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4 flex items-center space-x-2">
                  <FileText className="w-5 h-5 text-primary-600" />
                  <span>Abstract</span>
                </h2>
                <p className="text-gray-700 dark:text-gray-300 leading-relaxed whitespace-pre-wrap">
                  {asset.description}
                </p>
              </div>
            )}

            {/* Classifications */}
            {(asset.ipcClassification || asset.cpcClassification) && (
              <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
                <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
                  Classifications
                </h2>
                <div className="space-y-3">
                  {asset.ipcClassification && (
                    <div>
                      <p className="text-sm text-gray-500 dark:text-gray-400 mb-1">IPC Classification</p>
                      <p className="font-mono text-sm text-gray-900 dark:text-white bg-gray-50 dark:bg-gray-700 px-3 py-2 rounded">
                        {asset.ipcClassification}
                      </p>
                    </div>
                  )}
                  {asset.cpcClassification && (
                    <div>
                      <p className="text-sm text-gray-500 dark:text-gray-400 mb-1">CPC Classification</p>
                      <p className="font-mono text-sm text-gray-900 dark:text-white bg-gray-50 dark:bg-gray-700 px-3 py-2 rounded">
                        {asset.cpcClassification}
                      </p>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Keywords */}
            {asset.keywords && (
              <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
                <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
                  Keywords
                </h2>
                <div className="flex flex-wrap gap-2">
                  {asset.keywords.split(',').map((keyword, index) => (
                    <span
                      key={index}
                      className="px-3 py-1 bg-primary-100 dark:bg-primary-900/30 text-primary-700 dark:text-primary-300 rounded-full text-sm"
                    >
                      {keyword.trim()}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Parties */}
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                Parties
              </h2>
              <div className="space-y-4">
                {asset.assignee && (
                  <div>
                    <div className="flex items-center space-x-2 text-gray-500 dark:text-gray-400 mb-1">
                      <Building2 className="w-4 h-4" />
                      <span className="text-sm">Assignee/Owner</span>
                    </div>
                    <p className="text-gray-900 dark:text-white font-medium">{asset.assignee}</p>
                  </div>
                )}
                {asset.inventor && (
                  <div>
                    <div className="flex items-center space-x-2 text-gray-500 dark:text-gray-400 mb-1">
                      <User className="w-4 h-4" />
                      <span className="text-sm">Inventor</span>
                    </div>
                    <p className="text-gray-900 dark:text-white font-medium">{asset.inventor}</p>
                  </div>
                )}
              </div>
            </div>

            {/* Jurisdiction */}
            {asset.jurisdiction && (
              <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                  Jurisdiction
                </h2>
                <div className="flex items-center space-x-2">
                  <MapPin className="w-5 h-5 text-primary-600" />
                  <span className="text-gray-900 dark:text-white font-medium text-lg">
                    {asset.jurisdiction}
                  </span>
                </div>
              </div>
            )}

            {/* Important Dates */}
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                Important Dates
              </h2>
              <div className="space-y-3">
                {asset.priorityDate && (
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500 dark:text-gray-400">Priority Date</span>
                    <span className="text-sm font-medium text-gray-900 dark:text-white">
                      {new Date(asset.priorityDate).toLocaleDateString()}
                    </span>
                  </div>
                )}
                {asset.applicationDate && (
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500 dark:text-gray-400">Application Date</span>
                    <span className="text-sm font-medium text-gray-900 dark:text-white">
                      {new Date(asset.applicationDate).toLocaleDateString()}
                    </span>
                  </div>
                )}
                {asset.publicationDate && (
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500 dark:text-gray-400">Publication Date</span>
                    <span className="text-sm font-medium text-gray-900 dark:text-white">
                      {new Date(asset.publicationDate).toLocaleDateString()}
                    </span>
                  </div>
                )}
                {asset.grantDate && (
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500 dark:text-gray-400">Grant Date</span>
                    <span className="text-sm font-medium text-gray-900 dark:text-white">
                      {new Date(asset.grantDate).toLocaleDateString()}
                    </span>
                  </div>
                )}
              </div>
            </div>

            {/* Legal Status */}
            {asset.legalStatus && (
              <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                  Legal Status
                </h2>
                <p className="text-gray-700 dark:text-gray-300">{asset.legalStatus}</p>
              </div>
            )}

            {/* External Links */}
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                External Resources
              </h2>
              <div className="space-y-2">
                {asset.patentOffice === 'USPTO' && asset.publicationNumber && (
                  <a
                    href={`https://patents.google.com/patent/${asset.publicationNumber}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 rounded-lg transition-colors group"
                  >
                    <span className="text-sm font-medium text-gray-900 dark:text-white">
                      View on Google Patents
                    </span>
                    <ExternalLink className="w-4 h-4 text-gray-400 group-hover:text-primary-600" />
                  </a>
                )}
                {asset.patentOffice && asset.publicationNumber && (
                  <a
                    href={`https://worldwide.espacenet.com/patent/search?q=${asset.publicationNumber}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 rounded-lg transition-colors group"
                  >
                    <span className="text-sm font-medium text-gray-900 dark:text-white">
                      View on Espacenet
                    </span>
                    <ExternalLink className="w-4 h-4 text-gray-400 group-hover:text-primary-600" />
                  </a>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
