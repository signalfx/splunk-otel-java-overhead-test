async function toggleHistorical() {
    console.log("Toggle historical view...")
    const sel = document.getElementById('test-run');
    //currentUrl.searchParams.delete(name);
    // sel.disabled = !sel.disabled;
    if (sel.classList.contains('d-none')) {
        sel.classList.remove('d-none');
    } else {
        sel.classList.add('d-none');
    }

    const configs = await getAllRunConfigs();
    configs.sort((a,b) => a.run.localeCompare(b.run));
    // console.log(configs);
    const configsWithResults = await addResults(configs);
    console.log('everything:');
    console.log(configsWithResults);
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
    console.log(seriesData);
    new Chartist.Line(`#${resultsType}-chart`, {
        labels: labels,
        series: seriesData
    }, {
        fullWidth: true,
        chartPadding: {
            right: 40
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
