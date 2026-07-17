import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { baseUrl, constantLoad, ensureAccount, statusIsSuccess } from './common.js';
import http from 'k6/http';

export const options = constantLoad;
const loginDuration = new Trend('login_response_time', true);
const loginSuccess = new Rate('login_success_rate');

export default function () {
  const user = ensureAccount();
  const response = http.post(`${baseUrl}/login`, { email: user.email, password: user.password }, { tags: { endpoint: 'login' } });
  loginDuration.add(response.timings.duration);
  loginSuccess.add(statusIsSuccess(response));
  check(response, { 'login returns success': statusIsSuccess });
  sleep(1);
}

export function handleSummary(data) {
  return { [`${__ENV.REPORT_DIR || 'k6/reports'}/login-summary.json`]: JSON.stringify(data, null, 2) };
}
