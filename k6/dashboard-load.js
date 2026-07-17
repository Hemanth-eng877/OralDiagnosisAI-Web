import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { baseUrl, constantLoad, login, statusIsSuccess } from './common.js';
import http from 'k6/http';

export const options = constantLoad;
const dashboardDuration = new Trend('dashboard_response_time', true);
const reportsDuration = new Trend('reports_response_time', true);
const dashboardSuccess = new Rate('dashboard_success_rate');
const reportsSuccess = new Rate('reports_success_rate');

export default function () {
  login();
  const response = http.get(`${baseUrl}/dashboard`, { tags: { endpoint: 'dashboard' } });
  dashboardDuration.add(response.timings.duration);
  dashboardSuccess.add(statusIsSuccess(response));
  check(response, { 'dashboard returns success': statusIsSuccess, 'dashboard renders overview': (r) => r.body.includes('Clinical Overview') });
  const reports = http.get(`${baseUrl}/reports?q=k6`, { tags: { endpoint: 'reports' } });
  reportsDuration.add(reports.timings.duration);
  reportsSuccess.add(statusIsSuccess(reports));
  check(reports, { 'reports returns success': statusIsSuccess, 'reports page renders': (r) => r.body.includes('Diagnosis History') });
  sleep(1);
}

export function handleSummary(data) {
  return { [`${__ENV.REPORT_DIR || 'k6/reports'}/dashboard-summary.json`]: JSON.stringify(data, null, 2) };
}
