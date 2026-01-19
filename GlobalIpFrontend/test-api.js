const axios = require('axios');

// Test credentials
const consumerKey = 'RhLoojvEdsHohkcZnGheZ8GwVYijlQNvxis3QbDuXODxMVB5';
const consumerSecret = 'R3XbaxqlJgY1BmLo18R1ObenXdY7iEvHcriKXQ9UlG5fGaHmg0pRGkKjXIwAizm';

// Test 1: Basic connectivity test
console.log('🧪 Testing API connectivity...\n');

const testEndpoints = [
  {
    name: 'Health Check',
    url: 'http://localhost:8080/health',
    method: 'GET',
    useAuth: false
  },
  {
    name: 'OAuth Token Request',
    url: 'http://localhost:8080/oauth/token',
    method: 'POST',
    useAuth: true,
    data: {
      grant_type: 'client_credentials',
      client_id: consumerKey,
      client_secret: consumerSecret
    }
  },
  {
    name: 'Login with OAuth Credentials',
    url: 'http://localhost:8080/api/auth/login',
    method: 'POST',
    useAuth: true,
    data: {
      consumerKey: consumerKey,
      consumerSecret: consumerSecret
    }
  },
  {
    name: 'API Status',
    url: 'http://localhost:8080/api/status',
    method: 'GET',
    useAuth: true
  }
];

async function testEndpoint(endpoint) {
  try {
    const config = {
      method: endpoint.method,
      url: endpoint.url,
      headers: {
        'Content-Type': 'application/json'
      }
    };

    if (endpoint.useAuth) {
      const credentials = Buffer.from(`${consumerKey}:${consumerSecret}`).toString('base64');
      config.headers['Authorization'] = `Basic ${credentials}`;
    }

    if (endpoint.data) {
      config.data = endpoint.data;
    }

    console.log(`\n📍 Testing: ${endpoint.name}`);
    console.log(`   URL: ${endpoint.url}`);
    console.log(`   Method: ${endpoint.method}`);

    const response = await axios(config);

    console.log(`   ✅ Status: ${response.status}`);
    console.log(`   Response Data:`, JSON.stringify(response.data, null, 2));
    
    return { success: true, endpoint: endpoint.name, status: response.status, data: response.data };

  } catch (error) {
    if (error.response) {
      console.log(`   ❌ Error Status: ${error.response.status}`);
      console.log(`   Error Response:`, JSON.stringify(error.response.data, null, 2));
      return { success: false, endpoint: endpoint.name, status: error.response.status, error: error.response.data };
    } else if (error.code === 'ECONNREFUSED') {
      console.log(`   ❌ Connection Refused - Server not running on ${error.address}:${error.port}`);
      return { success: false, endpoint: endpoint.name, error: 'ECONNREFUSED', message: 'Could not connect to API server' };
    } else {
      console.log(`   ❌ Error: ${error.message}`);
      return { success: false, endpoint: endpoint.name, error: error.message };
    }
  }
}

async function runTests() {
  const results = [];
  
  for (const endpoint of testEndpoints) {
    const result = await testEndpoint(endpoint);
    results.push(result);
    // Add delay between requests
    await new Promise(resolve => setTimeout(resolve, 500));
  }

  // Summary
  console.log('\n\n📊 Test Summary:');
  console.log('='.repeat(60));
  const successful = results.filter(r => r.success).length;
  const failed = results.filter(r => !r.success).length;
  
  console.log(`✅ Successful: ${successful}`);
  console.log(`❌ Failed: ${failed}`);
  
  console.log('\nDetailed Results:');
  results.forEach(result => {
    const status = result.success ? '✅' : '❌';
    console.log(`${status} ${result.endpoint} - ${result.status || 'N/A'}`);
  });

  console.log('\n' + '='.repeat(60));
  console.log('API Test Complete!\n');
}

runTests().catch(console.error);
