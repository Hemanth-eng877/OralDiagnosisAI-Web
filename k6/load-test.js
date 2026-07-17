import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 100,
  duration: '1m',
};

export default function () {
  const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:5000';
  const email = `load${Math.random().toString(36).slice(2)}@example.com`;
  const password = 'SecurePass123';

  const signupRes = http.post(`${baseUrl}/signup`, {
    name: 'Load Tester',
    email,
    password,
  });
  check(signupRes, { 'signup status is 200 or 302': (r) => r.status === 200 || r.status === 302 });

  const loginRes = http.post(`${baseUrl}/login`, {
    email,
    password,
  });
  check(loginRes, { 'login status is 200 or 302': (r) => r.status === 200 || r.status === 302 });

  const dashboardRes = http.get(`${baseUrl}/dashboard`);
  check(dashboardRes, { 'dashboard status is 200': (r) => r.status === 200 });

  const patientsRes = http.get(`${baseUrl}/patients`);
  check(patientsRes, { 'patients status is 200': (r) => r.status === 200 });

  const diagnoseRes = http.get(`${baseUrl}/diagnose`);
  check(diagnoseRes, { 'diagnose page status is 200': (r) => r.status === 200 });

  const reportsRes = http.get(`${baseUrl}/reports`);
  check(reportsRes, { 'reports status is 200': (r) => r.status === 200 });

  sleep(1);
}
