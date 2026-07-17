import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import encoding from 'k6/encoding';
import http from 'k6/http';
import { baseUrl, constantLoad, ensurePatient, login, statusIsSuccess } from './common.js';

export const options = constantLoad;
const uploadDuration = new Trend('image_upload_response_time', true);
const uploadSuccess = new Rate('image_upload_success_rate');
const imageBytes = encoding.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIHWP4z8DwHwAFgAI/ScLw8QAAAABJRU5ErkJggg==');

export default function () {
  login();
  const patientId = ensurePatient();
  if (!patientId) return;
  const payload = {
    patient_id: patientId,
    image: http.file(imageBytes, 'k6-load-test.png', 'image/png'),
  };
  const response = http.post(`${baseUrl}/diagnose`, payload, { tags: { endpoint: 'image_upload' } });
  uploadDuration.add(response.timings.duration);
  uploadSuccess.add(statusIsSuccess(response));
  check(response, { 'image upload returns success': statusIsSuccess, 'diagnosis page renders': (r) => r.body.includes('Diagnosis') });
  sleep(1);
}

export function handleSummary(data) {
  return { [`${__ENV.REPORT_DIR || 'k6/reports'}/upload-summary.json`]: JSON.stringify(data, null, 2) };
}
