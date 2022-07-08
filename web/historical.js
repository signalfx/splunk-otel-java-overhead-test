async function toggleHistorical() {
    console.log("Toggle historical view...")
    const testDropDown = document.getElementById('test-run');
    // currentUrl.searchParams.delete(name);
    // sel.disabled = !sel.disabled;
    let historicalCurrentlyShown = testDropDown.classList.contains('d-none');
    if (historicalCurrentlyShown) {
        testDropDown.classList.remove('d-none');
        await testRunChosen();
        document.querySelectorAll('.ct-legend').forEach(x => x.style.display = 'none');
    } else {
        testDropDown.classList.add('d-none');
        updateUrlForHistorical();
        await showHistorical();
    }
}

async function showHistorical() {
    const configs = await getAllRunConfigs();
    configs.sort((a, b) => a.run.localeCompare(b.run));
    // console.log(configs);
    const configsWithResults = await addResults(configs);
    // console.log('everything:');
    // console.log(configsWithResults);
    addHistoricalOverview(configsWithResults);
    addHistoricalCharts(configsWithResults);
    setTimeout(tiltLabels, 1);
}

function addHistoricalOverview(configsWithResults) {
    const overview = document.getElementById('overview');
    overview.innerHTML = 'Showing historical comparison view';
}

function addHistoricalCharts(configsWithResults) {
    makeHistoricalChart(configsWithResults, 'startupDurationMs', "Seconds", x => x / 1000);
    makeHistoricalChart(configsWithResults, 'averageCpuUser', "% CPU load");
    makeHistoricalChart(configsWithResults, 'maxCpuUser', "% CPU load");
    makeHistoricalChart(configsWithResults, 'maxHeapUsed', "Megabytes", x => x / (1024 * 1024));
    makeHistoricalChart(configsWithResults, 'totalAllocatedMB', "Gigabytes", x => x / (1024));
    makeHistoricalChart(configsWithResults, 'totalGCTime', "Seconds", x => x / (1000 * 1000 * 1000));
    makeHistoricalChart(configsWithResults, 'gcPauseMs', "Milliseconds");
    makeHistoricalChart(configsWithResults, 'iterationAvg', "Milliseconds");
    makeHistoricalChart(configsWithResults, 'iterationP95', "Milliseconds");
    makeHistoricalChart(configsWithResults, 'requestAvg', "Milliseconds");
    makeHistoricalChart(configsWithResults, 'requestP95', "Milliseconds");
    makeHistoricalChart(configsWithResults, 'netReadAvg', "MiB/s", x => x / (1024 * 1024));
    makeHistoricalChart(configsWithResults, 'netWriteAvg', "MiB/s", x => x / (1024 * 1024));
    makeHistoricalChart(configsWithResults, 'peakThreadCount', "MiB/s");
    makeHistoricalChart(configsWithResults, 'maxThreadContextSwitchRate', "Switches per second");
    makeHistoricalChart(configsWithResults, 'runDurationMs', "Seconds", x => x / 1000);

}

function makeHistoricalChart(configsWithResults, resultsType, axisTitle, scaleFunction = x => x) {

    const results = configsWithResults.map(config => {
        const res = config.results.results[resultsType];
        return [config.run, res];
    });
    // console.log(results);
    const agents = allAgents(configsWithResults);
    const standardAgents = agents.filter(agent => !agent.includes(':'));
    // console.log(agents);
    const groupedByAgent = standardAgents.map(agent => {
        return [agent, results.map(result => {
           return result[1][agent];
        })];
    });

    // console.log(groupedByAgent);
    const data = groupedByAgent.filter(seriesIsEmpty);
    // console.log(data);

    const labels = results.map(x => x[0]);
    const seriesData = data.map(x => ({"name": x[0], "data": x[1]}));
    // console.log(seriesData);
    seriesData.forEach(series => {
        series.data = series.data.map(scaleFunction);
    });
    new Chartist.Line(`#${resultsType}-chart`, {
        labels: labels,
        series: seriesData
    }, {
        fullWidth: true,
        chartPadding: {
            left: 40,
            right: 40,
            top: 40
        },
        axisY: {
            low: 0
        },
        lineSmooth: Chartist.Interpolation.none({
            fillHoles: false
        }),
        plugins: [
            makeChartistAxisTitle(axisTitle),
            Chartist.plugins.legend({
                // position: 'bottom' doesn't work meh
            })
        ]
    });
}

function seriesIsEmpty(pair){
    const series = pair[1];
    return series.filter(x => x !== undefined).length > 0;
}

function allAgents(configsWithResults){
    const result = new Set();
    configsWithResults.forEach(config => {
       (config.agents || []).forEach( agent => result.add(agent.name));
       config.results.agents.forEach(agent => result.add(agent));
    });
    return [...result];
}

async function getAllRunConfigs(){
    const runs = await getRuns();
    const runPromises = runs.map(
        run => getConfig(run)
            .then(config => {
                config['run'] = run;
                return config;
            })
    );
    return Promise.all(runPromises);
}

async function addResults(configs) {
    const resultsPromises = configs.map(config => {
       return getResults(config.run)
           .then(results => {
               config['results'] = results;
               return config;
           });
    });
    return Promise.all(resultsPromises);
}
