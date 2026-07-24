const fs = require('fs');
const path = require('path');

// Defensive metric extraction to support various k6 summary schemas
function getMetricValue(metrics, metricName, property) {
    if (!metrics || !metrics[metricName]) return 'N/A';
    
    const metric = metrics[metricName];
    if (metric.values && metric.values[property] !== undefined) {
        return metric.values[property];
    }
    if (metric[property] !== undefined) {
        return metric[property];
    }
    return 'N/A';
}

function formatDuration(value) {
    if (value === 'N/A') return 'N/A';
    return \`\${parseFloat(value).toFixed(2)} ms\`;
}

function formatRate(value) {
    if (value === 'N/A') return 'N/A';
    return \`\${(parseFloat(value) * 100).toFixed(2)}%\`;
}

function formatBytes(value) {
    if (value === 'N/A') return 'N/A';
    const mb = parseFloat(value) / (1024 * 1024);
    return \`\${mb.toFixed(2)} MB\`;
}

function parseSummary() {
    console.log('Parsing k6 summary.json...');
    const summaryPath = path.join(process.cwd(), 'summary.json');
    
    if (!fs.existsSync(summaryPath)) {
        console.error('Error: summary.json not found!');
        process.exit(1);
    }

    let summaryData;
    try {
        summaryData = JSON.parse(fs.readFileSync(summaryPath, 'utf8'));
    } catch (e) {
        console.error('Error: Failed to parse summary.json', e.message);
        process.exit(1);
    }

    const metrics = summaryData.metrics || {};

    // Extract metrics defensively
    const totalRequests = getMetricValue(metrics, 'http_reqs', 'count');
    const rps = getMetricValue(metrics, 'http_reqs', 'rate');
    const avgResponseTime = getMetricValue(metrics, 'http_req_duration', 'avg');
    const minResponseTime = getMetricValue(metrics, 'http_req_duration', 'min');
    const maxResponseTime = getMetricValue(metrics, 'http_req_duration', 'max');
    const medResponseTime = getMetricValue(metrics, 'http_req_duration', 'med');
    const p90ResponseTime = getMetricValue(metrics, 'http_req_duration', 'p(90)');
    const p95ResponseTime = getMetricValue(metrics, 'http_req_duration', 'p(95)');
    
    const failRate = getMetricValue(metrics, 'http_req_failed', 'rate');
    const checkPassRate = getMetricValue(metrics, 'checks', 'rate');
    
    const dataSent = getMetricValue(metrics, 'data_sent', 'count');
    const dataRecv = getMetricValue(metrics, 'data_received', 'count');

    // Evaluate Result (against basic thresholds)
    let overallResult = 'PASS';
    if (failRate !== 'N/A' && parseFloat(failRate) >= 0.05) overallResult = 'FAIL';
    if (p95ResponseTime !== 'N/A' && parseFloat(p95ResponseTime) >= 1500) overallResult = 'FAIL';
    if (avgResponseTime !== 'N/A' && parseFloat(avgResponseTime) >= 800) overallResult = 'FAIL';

    // Generate Markdown Report
    const reportMd = 
\`## API Performance Load Test Summary

| Metric | Value |
| --- | --- |
| **Total Requests** | \${totalRequests} |
| **Requests per Second** | \${parseFloat(rps).toFixed(2)} req/s |
| **Average Response Time** | \${formatDuration(avgResponseTime)} |
| **Minimum Response Time** | \${formatDuration(minResponseTime)} |
| **Maximum Response Time** | \${formatDuration(maxResponseTime)} |
| **Median Response Time** | \${formatDuration(medResponseTime)} |
| **p(90) Latency** | \${formatDuration(p90ResponseTime)} |
| **p(95) Latency** | \${formatDuration(p95ResponseTime)} |
| **Error Rate** | \${formatRate(failRate)} |
| **Check Pass Rate** | \${formatRate(checkPassRate)} |
| **Data Sent** | \${formatBytes(dataSent)} |
| **Data Received** | \${formatBytes(dataRecv)} |
| **Overall Result** | **\${overallResult}** |

> Thresholds evaluated: Error Rate < 5%, p(95) Latency < 1500ms, Avg Latency < 800ms.
\`;

    fs.writeFileSync('performance-summary.md', reportMd);
    console.log(reportMd);

    // Append to GITHUB_STEP_SUMMARY if available
    const stepSummaryPath = process.env.GITHUB_STEP_SUMMARY;
    if (stepSummaryPath) {
        const buildInfo = \`### Build Information\\n- **Build Number**: \${process.env.GITHUB_RUN_NUMBER || 'Local'}\\n- **Commit SHA**: \${process.env.GITHUB_SHA || 'N/A'}\\n\\n\`;
        fs.appendFileSync(stepSummaryPath, buildInfo + reportMd + '\\n');
    }
}

parseSummary();
