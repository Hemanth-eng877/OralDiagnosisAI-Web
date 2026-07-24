const fs = require('fs');
const path = require('path');
const ExcelJS = require('exceljs');

const REPORT_DIR = process.env.REPORT_DIR || path.join(__dirname, '..', 'Vulnerability Test Results');

// Ensure output directory exists
if (!fs.existsSync(REPORT_DIR)) {
    fs.mkdirSync(REPORT_DIR, { recursive: true });
}

function loadJson(filename, defaultValue = {}) {
    const filepath = path.join(REPORT_DIR, filename);
    if (!fs.existsSync(filepath)) return defaultValue;
    try {
        return JSON.parse(fs.readFileSync(filepath, 'utf8'));
    } catch (e) {
        console.error(`Error parsing ${filename}:`, e.message);
        return defaultValue;
    }
}

async function generateReports() {
    console.log('Ingesting security scanner outputs...');
    
    const findings = [];
    const dependencyFindings = [];

    // 1. Parse Semgrep
    const semgrep = loadJson('semgrep.json', { results: [] });
    (semgrep.results || []).forEach(res => {
        let severity = 'LOW';
        const semgrepSev = res.extra?.severity || '';
        if (semgrepSev === 'ERROR') severity = 'CRITICAL';
        if (semgrepSev === 'WARNING') severity = 'MEDIUM';
        
        findings.push({
            severity: severity,
            file: res.path || 'Unknown',
            line: res.start?.line || 'N/A',
            category: res.check_id || 'Static Analysis',
            description: res.extra?.message || 'No description provided.',
            recommendation: 'Review code logic and apply secure coding practices.',
            tool: 'Semgrep'
        });
    });

    // 2. Parse Gitleaks
    const gitleaks = loadJson('gitleaks.json', []);
    (Array.isArray(gitleaks) ? gitleaks : []).forEach(res => {
        findings.push({
            severity: 'CRITICAL',
            file: res.File || 'Unknown',
            line: res.StartLine || 'N/A',
            category: 'Hardcoded Secret',
            description: `Exposed secret detected: ${res.Description || res.RuleID}`,
            recommendation: 'Revoke the secret immediately and use environment variables/secret management.',
            tool: 'Gitleaks'
        });
    });

    // 3. Parse Trivy
    const trivy = loadJson('trivy.json', { Results: [] });
    (trivy.Results || []).forEach(target => {
        (target.Vulnerabilities || []).forEach(vuln => {
            const entry = {
                severity: vuln.Severity ? vuln.Severity.toUpperCase() : 'MEDIUM',
                file: target.Target || 'Unknown',
                line: 'N/A',
                category: vuln.VulnerabilityID || 'Vulnerability',
                description: vuln.Title || vuln.Description || 'No description',
                recommendation: vuln.FixedVersion ? `Upgrade to ${vuln.FixedVersion}` : 'Monitor for patches',
                tool: 'Trivy'
            };
            findings.push(entry);
            dependencyFindings.push(entry);
        });
    });

    // 4. Parse npm audit
    const npmAudit = loadJson('npm-audit.json', { vulnerabilities: {} });
    Object.values(npmAudit.vulnerabilities || {}).forEach(vuln => {
        const entry = {
            severity: vuln.severity ? vuln.severity.toUpperCase() : 'LOW',
            file: 'package.json / package-lock.json',
            line: 'N/A',
            category: `Dependency: ${vuln.name}`,
            description: `Vulnerable versions: ${vuln.range}`,
            recommendation: `Run npm audit fix or upgrade ${vuln.name}`,
            tool: 'npm audit'
        };
        findings.push(entry);
        dependencyFindings.push(entry);
    });

    // Summary Statistics
    const stats = { CRITICAL: 0, HIGH: 0, MEDIUM: 0, LOW: 0, TOTAL: findings.length };
    findings.forEach(f => {
        if (stats[f.severity] !== undefined) stats[f.severity]++;
    });

    console.log('Generating Excel Report...');
    const workbook = new ExcelJS.Workbook();
    
    // Sheet 1: Security Findings
    const findingsSheet = workbook.addWorksheet('Security Findings');
    findingsSheet.columns = [
        { header: 'Severity', key: 'severity', width: 15 },
        { header: 'Tool', key: 'tool', width: 15 },
        { header: 'File', key: 'file', width: 30 },
        { header: 'Line', key: 'line', width: 10 },
        { header: 'Category', key: 'category', width: 25 },
        { header: 'Description', key: 'description', width: 50 },
        { header: 'Recommendation', key: 'recommendation', width: 50 }
    ];
    
    if (findings.length === 0) {
        findingsSheet.addRow({ description: 'No security issues detected.' });
    } else {
        findings.forEach(f => findingsSheet.addRow(f));
    }
    findingsSheet.getRow(1).font = { bold: true };

    // Sheet 2: Dependency Review
    const depSheet = workbook.addWorksheet('Dependency Review');
    depSheet.columns = [...findingsSheet.columns];
    if (dependencyFindings.length === 0) {
        depSheet.addRow({ description: 'No dependency issues detected.' });
    } else {
        dependencyFindings.forEach(d => depSheet.addRow(d));
    }
    depSheet.getRow(1).font = { bold: true };

    // Sheet 3: Risk Summary
    const summarySheet = workbook.addWorksheet('Risk Summary');
    summarySheet.columns = [
        { header: 'Severity Level', key: 'level', width: 20 },
        { header: 'Count', key: 'count', width: 15 }
    ];
    summarySheet.addRows([
        { level: 'CRITICAL', count: stats.CRITICAL },
        { level: 'HIGH', count: stats.HIGH },
        { level: 'MEDIUM', count: stats.MEDIUM },
        { level: 'LOW', count: stats.LOW },
        { level: 'TOTAL', count: stats.TOTAL }
    ]);
    summarySheet.getRow(1).font = { bold: true };

    await workbook.xlsx.writeFile(path.join(REPORT_DIR, 'findings.xlsx'));

    console.log('Generating Markdown Reports...');
    
    const execSummaryMd = 
`# Executive Security Summary
**Total Findings:** ${stats.TOTAL}
- **CRITICAL:** ${stats.CRITICAL}
- **HIGH:** ${stats.HIGH}
- **MEDIUM:** ${stats.MEDIUM}
- **LOW:** ${stats.LOW}

*Note: Workflows will fail if CRITICAL > 0.*
`;
    fs.writeFileSync(path.join(REPORT_DIR, 'executive-summary.md'), execSummaryMd);

    const formatFindingsMd = (arr) => arr.length === 0 ? "No issues detected.\n" : arr.map(f => `- **[${f.severity}]** ${f.tool}: ${f.category} in \`${f.file}\` (Line: ${f.line})\n  *Desc:* ${f.description}\n  *Fix:* ${f.recommendation}`).join('\n\n');

    const secReviewMd = `# Detailed Security Review\n\n${formatFindingsMd(findings)}`;
    fs.writeFileSync(path.join(REPORT_DIR, 'security-review.md'), secReviewMd);

    const depReportMd = `# Dependency Vulnerability Report\n\n${formatFindingsMd(dependencyFindings)}`;
    fs.writeFileSync(path.join(REPORT_DIR, 'dependency-report.md'), depReportMd);

    console.log('Generating HTML Dashboard...');
    const html = `
    <!DOCTYPE html>
    <html lang="en">
    <head><meta charset="UTF-8"><title>Security Dashboard</title>
    <style>
        body { font-family: sans-serif; background: #121212; color: #fff; padding: 20px; }
        .dashboard { display: flex; gap: 20px; margin-bottom: 20px; }
        .card { background: #1e1e1e; padding: 20px; border-radius: 8px; flex: 1; text-align: center; }
        .CRITICAL { color: #f44336; } .HIGH { color: #ff9800; } .MEDIUM { color: #ffeb3b; } .LOW { color: #4caf50; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 10px; border: 1px solid #333; text-align: left; }
        th { background: #333; }
    </style></head>
    <body>
        <h1>Security Execution Dashboard</h1>
        <div class="dashboard">
            <div class="card"><h2 class="CRITICAL">Critical</h2><p>${stats.CRITICAL}</p></div>
            <div class="card"><h2 class="HIGH">High</h2><p>${stats.HIGH}</p></div>
            <div class="card"><h2 class="MEDIUM">Medium</h2><p>${stats.MEDIUM}</p></div>
            <div class="card"><h2 class="LOW">Low</h2><p>${stats.LOW}</p></div>
        </div>
        <h2>All Findings</h2>
        <table>
            <tr><th>Severity</th><th>Tool</th><th>File</th><th>Category</th><th>Description</th></tr>
            ${findings.length === 0 ? '<tr><td colspan="5">No issues detected.</td></tr>' : findings.map(f => `<tr><td class="${f.severity}">${f.severity}</td><td>${f.tool}</td><td>${f.file}</td><td>${f.category}</td><td>${f.description}</td></tr>`).join('')}
        </table>
    </body></html>`;
    
    fs.writeFileSync(path.join(REPORT_DIR, 'execution-report.html'), html);

    console.log('All reports generated successfully.');

    // Write final status for GitHub Actions Gate
    fs.writeFileSync(path.join(REPORT_DIR, 'gate_status.txt'), stats.CRITICAL.toString());
}

generateReports().catch(console.error);
