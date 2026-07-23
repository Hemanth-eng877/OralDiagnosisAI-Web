import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 100 },
    { duration: '20s', target: 100 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'],
    http_req_failed: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:5000';

export default function () {
  const payload = JSON.stringify({
    email: `spike_${__VU}_${__ITER}@example.com`,
    password: 'Password123'
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(`${BASE_URL}/api/login`, payload, params);
  check(res, {
    'api login handles load (even if 401)': (r) => r.status === 200 || r.status === 401,
  });

  sleep(0.5);
}
