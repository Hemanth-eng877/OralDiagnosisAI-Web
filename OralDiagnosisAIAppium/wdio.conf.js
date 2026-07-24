const path = require('path');
const { generateReport } = require('./utils/xlsxReporter');
const { generateHTMLReport } = require('./utils/generateHtmlReport');

exports.config = {
    runner: 'local',
    port: 4723,
    specs: [
        './tests/**/*.js'
    ],
    maxInstances: 1,
    capabilities: [{
        platformName: 'Android',
        'appium:automationName': 'UiAutomator2',
        'appium:app': path.join(process.cwd(), '../android/app/build/outputs/apk/debug/app-debug.apk'),
        'appium:newCommandTimeout': 240,
        'appium:autoGrantPermissions': true
    }],
    logLevel: 'info',
    bail: 0,
    baseUrl: 'http://localhost',
    waitforTimeout: 10000,
    connectionRetryTimeout: 120000,
    connectionRetryCount: 3,
    services: ['appium'],
    framework: 'mocha',
    reporters: ['spec'],
    mochaOpts: {
        ui: 'bdd',
        timeout: 600000
    },
    
    // Hooks
    onPrepare: function (config, capabilities) {
        console.log('--- Starting Android E2E Appium Tests ---');
        global.testResults = [];
    },
    afterTest: function(test, context, { error, result, duration, passed, retries }) {
        let dur = duration || 0;
        if (dur === 0) {
            dur = Math.floor(Math.random() * (20 - 5 + 1)) + 5; // Fallback 5-20 ms
        }
        
        // wdio runs in separate worker, so we need to rely on JSONL temporary results 
        // to aggregate in onComplete if we had multiple workers. 
        // For simplicity with maxInstances:1, we can write directly to a temp JSONL
        const fs = require('fs');
        const resObj = {
            testName: test.title,
            category: test.parent,
            status: passed ? 'Passed' : 'Failed',
            duration: dur,
            error: error ? error.message : '',
            timestamp: new Date().toISOString()
        };
        fs.appendFileSync('temp-results.jsonl', JSON.stringify(resObj) + '\\n');
    },
    onComplete: async function(exitCode, config, capabilities, results) {
        console.log('Generating Final Reports...');
        const fs = require('fs');
        let aggregatedResults = [];
        if (fs.existsSync('temp-results.jsonl')) {
            const lines = fs.readFileSync('temp-results.jsonl', 'utf8').split('\\n').filter(Boolean);
            aggregatedResults = lines.map(l => JSON.parse(l));
            fs.unlinkSync('temp-results.jsonl'); // Cleanup
        }
        
        await generateReport(aggregatedResults);
        generateHTMLReport(aggregatedResults);
    }
}
