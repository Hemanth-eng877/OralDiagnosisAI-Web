#!/bin/bash

echo "Starting Android Appium CI Pipeline..."

# Move into Appium directory
cd OralDiagnosisAIAppium || exit 1

echo "Installing dependencies..."
npm install

echo "Starting Appium Server in background..."
npx appium &
APPIUM_PID=$!

# Wait for Appium to bind
sleep 5

echo "Executing WebDriverIO Test Suite..."
npx wdio run wdio.conf.js
EXIT_CODE=$?

# If the runner crashes or fails heavily, ensure fallback reports are still generated
if [ $EXIT_CODE -ne 0 ]; then
    echo "Tests failed or crashed. Ensuring fallback reports are generated from temp logs..."
    if [ -f "temp-results.jsonl" ]; then
        node -e "
            const { generateReport } = require('./utils/xlsxReporter');
            const { generateHTMLReport } = require('./utils/generateHtmlReport');
            const fs = require('fs');
            const lines = fs.readFileSync('temp-results.jsonl', 'utf8').split('\\n').filter(Boolean);
            const results = lines.map(l => JSON.parse(l));
            generateReport(results).then(() => generateHTMLReport(results));
        "
    else
        echo "No temp results found. Fallback report generation skipped."
    fi
fi

echo "Cleaning up Appium..."
kill -9 $APPIUM_PID || true

exit $EXIT_CODE
