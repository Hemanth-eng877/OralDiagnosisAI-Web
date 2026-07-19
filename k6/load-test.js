import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:5000';
const responseTime = new Trend('application_response_time', true);

export const options = {
  vus: 100,
  duration: '1m',
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<5000'],
    checks: ['rate>0.95'],
  },
};

let account;

function newAccount() {
  const suffix = `${__VU}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  return {
    email: `k6-load-${suffix}@example.com`,
    password: 'K6LoadTest!2026',
  };
}

function record(response, name, expectedStatus = 200) {
  responseTime.add(response.timings.duration, { endpoint: name });
  check(response, {
    [`${name} returns ${expectedStatus}`]: (result) => result.status === expectedStatus,
  });
}

function authenticate() {
  if (account) return;

  account = newAccount();
  const signup = http.post(`${baseUrl}/signup`, {
    name: 'k6 Load Tester',
    email: account.email,
    password: account.password,
  }, { redirects: 0, tags: { endpoint: 'signup' } });
  record(signup, 'signup', 302);

  const login = http.post(`${baseUrl}/login`, {
    email: account.email,
    password: account.password,
  }, { redirects: 0, tags: { endpoint: 'login' } });
  record(login, 'login', 302);
}

export default function () {
  authenticate();

  // These routes cover the public landing experience, authentication, diagnosis
  // (prediction) page, and diagnosis reports without executing model inference.
  const home = http.get(`${baseUrl}/`, { redirects: 0, tags: { endpoint: 'home' } });
  record(home, 'home', 302);

  const loginPage = http.get(`${baseUrl}/login`, { tags: { endpoint: 'login_page' } });
  record(loginPage, 'login page');

  const prediction = http.get(`${baseUrl}/diagnose`, { redirects: 0, tags: { endpoint: 'prediction' } });
  record(prediction, 'prediction endpoint');

  const reports = http.get(`${baseUrl}/reports`, { redirects: 0, tags: { endpoint: 'reports' } });
  record(reports, 'reports page');

  sleep(1);
}
