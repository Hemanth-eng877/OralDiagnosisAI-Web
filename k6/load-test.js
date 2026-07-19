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

function newAccount() {
  const suffix = `${__VU}-${Date.now()}-${Math.random()
    .toString(36)
    .substring(2, 8)}`;

  return {
    email: `k6-${suffix}@example.com`,
    password: "Password@123",
  };
}

export default function () {

  const jar = http.cookieJar();

  const account = newAccount();

  // Signup
  let res = http.post(
    `${baseUrl}/signup`,
    {
      name: "K6 User",
      email: account.email,
      password: account.password,
    },
    {
      redirects: 0,
      jar: jar,
    }
  );

  responseTime.add(res.timings.duration);

  check(res, {
    "signup success": (r) => r.status === 302 || r.status === 200,
  });

  // Login
  res = http.post(
    `${baseUrl}/login`,
    {
      email: account.email,
      password: account.password,
    },
    {
      redirects: 0,
      jar: jar,
    }
  );

  responseTime.add(res.timings.duration);

  check(res, {
    "login success": (r) => r.status === 302 || r.status === 200,
  });

  // Home
  res = http.get(`${baseUrl}/`, {
    jar: jar,
  });

  responseTime.add(res.timings.duration);

  check(res, {
    "home page": (r) => r.status === 200,
  });

  // Diagnose
  res = http.get(`${baseUrl}/diagnose`, {
    jar: jar,
  });

  responseTime.add(res.timings.duration);

  check(res, {
    "diagnose page": (r) => r.status === 200,
  });

  // Reports
  res = http.get(`${baseUrl}/reports`, {
    jar: jar,
  });

  responseTime.add(res.timings.duration);

  check(res, {
    "reports page": (r) => r.status === 200,
  });

  sleep(1);
}