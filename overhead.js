
const MARKETING_NAMES = {
    'none': 'Not instrumented',
    'splunk-otel': 'Splunk Java OTel agent',
    'profiler': 'Splunk Java OTel agent with AlwaysOn Profiling'
}

async function startOverhead() {
    console.log('overhead started');
    document.getElementById('test-run')
        .addEventListener("change", testRunChosen);
    getRuns()
        .then(runNames => {
            console.log(runNames);
            populateRunsDropDown(runNames);
            document.getElementById('test-run').value = runNames[0];
            testRunChosen(runNames[0]);
        });
}

async function testRunChosen() {
    //TODO: Consider clearing/removing existing graphs first
    const value = document.getElementById('test-run').value;
    console.log(`selection changed ${value}`);
    const config = await getConfig(value);
    const results = await getResults(value)
    addOverview(config);
    addCharts(results);
}

function addOverview(config) {
    const overview = document.getElementById('overview');
    overview.innerHTML = '';
    addMainOverview(overview, config);
    addAgents(overview, config);
}

function addMainOverview(overview, config) {
    const title = document.createElement('h4');
    if(!config.name){
        title.innerText = '<<unavailable>>';
        overview.append(title);
        return;
    }
    title.innerText = config.name;
    const desc = document.createElement('p');
    desc.innerText = config.description;
    const list = document.createElement('ul');

    addListItem(list, `<b>concurrent connections</b>: ${config.concurrentConnections}`);
    addListItem(list, `<b>max rate</b>: ${config.maxRequestRate} rps`);
    addListItem(list, `<b>script iterations</b>: ${config.totalIterations}`);
    addListItem(list, `<b>warmup</b>: ${config.warmupSeconds}s`);

    overview.append(title, desc, list);
}

function addAgents(overview, config) {
    if(!config.agents) return;
    config.agents.forEach(agent => {
        const card = document.createElement('div');
        card.classList.add('card', 'my-2');
        card.style = 'width: 25rem;';
        const body = document.createElement('div');
        body.classList.add('card-body');
        const title = document.createElement('h5')
        title.classList.add('card-title');
        const marketed = MARKETING_NAMES[agent.name] || '';
        title.innerText = `${marketed} (${agent.name})`;
        const subtitle = document.createElement('h6');
        subtitle.classList.add('card-subtitle', 'mb-2', 'text-muted');
        subtitle.innerText = agent.description;
        const iconLink = document.createElement('a');
        iconLink.classList.add('float-end', agent.url ? 'text-primary' : 'text-secondary');
        iconLink.href = agent.url || '#';
        const icon = document.createElement('i');
        icon.classList.add('bi', 'bi-bookmark-check-fill', 'mx-2');
        iconLink.append(icon);
        body.append(iconLink);
        body.append(title);
        body.append(subtitle);
        card.append(body);
        overview.append(card);
        if(agent.additionalJvmArgs.length > 0){
            const p = document.createElement('p');
            p.innerText = 'Extra JVM args:';
            body.append(p)
            const args = document.createElement('ul');
            agent.additionalJvmArgs.forEach(arg => {
                addListItem(args, arg, ['font-monospace', 'jvmarg']);
            });
            body.append(args);
        }
    });
}

function addListItem(list, text, classes = []) {
    const li = document.createElement('li')
    classes.forEach(c => li.classList.add(c));
    li.innerHTML = text;
    list.append(li);
}

function populateRunsDropDown(runNames) {
    const sel = document.getElementById('test-run');
    runNames.forEach(name => {
        const option = document.createElement("option");
        option.text = name;
        option.value = name;
        sel.add(option);
    });
}

function addCharts(aggregated) {
    makeChart(aggregated, 'startupDurationMs', "Seconds", x => x / 1000);
    makeChart(aggregated, 'averageCpuUser', "% CPU load");
    makeChart(aggregated, 'maxCpuUser', "% CPU load");
    makeChart(aggregated, 'maxHeapUsed', "Megabytes", x => x / (1024 * 1024));
    makeChart(aggregated, 'totalAllocatedMB', "Gigabytes", x => x / (1024));
    makeChart(aggregated, 'totalGCTime', "Seconds", x => x / (1000 * 1000 * 1000));
    makeChart(aggregated, 'gcPauseMs', "Milliseconds");
    makeChart(aggregated, 'iterationAvg', "Milliseconds");
    makeChart(aggregated, 'iterationP95', "Milliseconds");
    makeChart(aggregated, 'requestAvg', "Milliseconds");
    makeChart(aggregated, 'requestP95', "Milliseconds");
    makeChart(aggregated, 'netReadAvg', "MiB/s", x => x / (1024 * 1024));
    makeChart(aggregated, 'netWriteAvg', "MiB/s", x => x / (1024 * 1024));
    makeChart(aggregated, 'peakThreadCount', "MiB/s");
    makeChart(aggregated, 'maxThreadContextSwitchRate', "Switches per second");
    makeChart(aggregated, 'runDurationMs', "Seconds", x => x / 1000);
}

function makeMarketingNames(agents) {
    return agents.map(a => MARKETING_NAMES[a] || a);
}

function makeChart(aggregated, resultType, axisTitle, scaleFunction = x => x) {
    const agents = aggregated['agents'];
    const marketingNames = makeMarketingNames(agents);
    const initialResults = agents.map(agent => aggregated['results'][resultType][agent]);
    const results = initialResults.map(scaleFunction);
    new Chartist.Bar(`#${resultType}-chart`, {
            labels: marketingNames,
            series: [results]
        },
        {
            seriesBarDistance: 10,
            axisX: {
                offset: 60
            },
            axisY: {
                offset: 60,
                scaleMinSpace: 20
            },
            plugins: [
                Chartist.plugins.ctBarLabels({
                    labelClass: 'ct-bar-label',
                    labelInterpolationFnc: function (text) {
                        return text.toFixed(2);
                    }
                }),
                Chartist.plugins.ctAxisTitle({
                    axisY: {
                        axisTitle: axisTitle,
                        axisClass: "ct-axis-title",
                        offset: {
                            x: 0,
                            y: 15
                        },
                        flipTitle: true
                    }
                })
            ]
        },
    );
}
