import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '1m', target: 20 },
    { duration: '3m', target: 20 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<200'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:5000';

export default function () {
  const res = http.get(`${BASE_URL}/api/dashboard?user_id=user1`);
  check(res, {
    'dashboard api reachable': (r) => r.status === 401 || r.status === 200,
  });

  sleep(2);
}
