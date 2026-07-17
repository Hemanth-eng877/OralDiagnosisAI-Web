import http from 'k6/http';
import { check } from 'k6';

export const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:5000';

export const constantLoad = {
  scenarios: {
    constant_load: {
      executor: 'constant-vus',
      vus: 100,
      duration: '1m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<2000'],
  },
};

let account;
let patientId;

function uniqueValue(prefix) {
  return `${prefix}-${__VU}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}

export function ensureAccount() {
  if (account) return account;
  const email = `${uniqueValue('k6')}@example.com`;
  const password = 'K6LoadTest!2026';
  const signup = http.post(`${baseUrl}/signup`, { name: 'k6 Load Tester', email, password }, { tags: { endpoint: 'signup_setup' } });
  check(signup, { 'setup signup succeeds': (r) => r.status === 200 || r.status === 302 });
  account = { email, password };
  return account;
}

export function login() {
  const user = ensureAccount();
  const response = http.post(`${baseUrl}/login`, { email: user.email, password: user.password }, { tags: { endpoint: 'login' } });
  check(response, { 'login succeeds': (r) => r.status === 200 || r.status === 302 });
  return response;
}

export function ensurePatient() {
  if (patientId) return patientId;
  login();
  const name = uniqueValue('Load Patient');
  const create = http.post(`${baseUrl}/patients/add`, {
    full_name: name,
    age: '42',
    gender: 'Other',
    phone: '5550100100',
    email: `${uniqueValue('patient')}@example.com`,
    notes: 'Synthetic k6 load-test record. Do not use for clinical care.',
  }, { tags: { endpoint: 'patient_create_setup' } });
  check(create, { 'patient setup succeeds': (r) => r.status === 200 || r.status === 302 });
  const diagnose = http.get(`${baseUrl}/diagnose`, { tags: { endpoint: 'diagnose_setup' } });
  const escapedName = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const match = diagnose.body.match(new RegExp(`<option value="([^"]+)">${escapedName}</option>`));
  check(diagnose, { 'patient id is available for upload': () => match !== null });
  patientId = match ? match[1] : null;
  return patientId;
}

export function statusIsSuccess(response) {
  return response.status >= 200 && response.status < 400;
}
