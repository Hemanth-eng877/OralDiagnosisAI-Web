import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { baseUrl, constantLoad, login, statusIsSuccess } from './common.js';
import http from 'k6/http';

export const options = constantLoad;
const patientCreateDuration = new Trend('patient_create_response_time', true);
const patientListDuration = new Trend('patient_list_response_time', true);
const patientSuccess = new Rate('patient_success_rate');

export default function () {
  login();
  const suffix = `${__VU}-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`;
  const create = http.post(`${baseUrl}/patients/add`, {
    full_name: `k6 Patient ${suffix}`,
    age: '42',
    gender: 'Other',
    phone: '5550100100',
    email: `patient-${suffix}@example.com`,
    notes: 'Synthetic k6 load-test record.',
  }, { tags: { endpoint: 'patient_create' } });
  patientCreateDuration.add(create.timings.duration);
  const list = http.get(`${baseUrl}/patients`, { tags: { endpoint: 'patient_list' } });
  patientListDuration.add(list.timings.duration);
  patientSuccess.add(statusIsSuccess(create) && statusIsSuccess(list));
  check(create, { 'patient create returns success': statusIsSuccess });
  check(list, { 'patient list returns success': statusIsSuccess, 'patient list renders records': (r) => r.body.includes('Patient Records') });
  sleep(1);
}

export function handleSummary(data) {
  return { [`${__ENV.REPORT_DIR || 'k6/reports'}/patient-summary.json`]: JSON.stringify(data, null, 2) };
}
