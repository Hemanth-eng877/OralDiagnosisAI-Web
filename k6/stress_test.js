import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:5000';

export default function () {
  const res = http.get(`${BASE_URL}/`);
  check(res, {
    'status is 200 or 302': (r) => r.status === 200 || r.status === 302,
  });

  const loginRes = http.get(`${BASE_URL}/login`);
  check(loginRes, {
    'login page loads': (r) => r.status === 200,
  });

  sleep(1);
}
