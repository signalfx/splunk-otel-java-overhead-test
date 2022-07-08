
async function startOverhead() {
    console.log('overhead started');
    document.getElementById('test-run')
        .addEventListener("change", testRunChosen);
    getRuns()
        .then(runNames => {
            console.log(runNames);
            populateRunsDropDown(runNames);

            const urlResultId = getResultIdFromUrl();
            let selectedResult = runNames[0];
            if(urlResultId && runNames.includes(urlResultId)){
                selectedResult = urlResultId;
            }
            document.getElementById('test-run').value = selectedResult;
            testRunChosen();
        });
}

async function testRunChosen() {
    //TODO: Consider clearing/removing existing graphs first
    const value = document.getElementById('test-run').value;
    console.log(`selection changed ${value}`);
    const config = await getConfig(value);
    const results = await getResults(value)
    addOverview(config);
    addCharts(results, config);
    updateUrl(value);
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
        title.innerText = agent.description;
        const subtitle = document.createElement('h6');
        subtitle.classList.add('card-subtitle', 'mb-2', 'text-muted');
        const versionStr = agent.version || 'unknown version'
        subtitle.innerText = `${agent.name} (${versionStr})`;
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

function addCharts(aggregated, config) {
    makeChart(aggregated, config, 'startupDurationMs', "Seconds", x => x / 1000);
    makeChart(aggregated, config, 'averageCpuUser', "% CPU load");
    makeChart(aggregated, config, 'maxCpuUser', "% CPU load");
    makeChart(aggregated, config, 'maxHeapUsed', "Megabytes", x => x / (1024 * 1024));
    makeChart(aggregated, config, 'totalAllocatedMB', "Gigabytes", x => x / (1024));
    makeChart(aggregated, config, 'totalGCTime', "Seconds", x => x / (1000 * 1000 * 1000));
    makeChart(aggregated, config, 'gcPauseMs', "Milliseconds");
    makeChart(aggregated, config, 'iterationAvg', "Milliseconds");
    makeChart(aggregated, config, 'iterationP95', "Milliseconds");
    makeChart(aggregated, config, 'requestAvg', "Milliseconds");
    makeChart(aggregated, config, 'requestP95', "Milliseconds");
    makeChart(aggregated, config, 'netReadAvg', "MiB/s", x => x / (1024 * 1024));
    makeChart(aggregated, config, 'netWriteAvg', "MiB/s", x => x / (1024 * 1024));
    makeChart(aggregated, config, 'peakThreadCount', "MiB/s");
    makeChart(aggregated, config, 'maxThreadContextSwitchRate', "Switches per second");
    makeChart(aggregated, config, 'runDurationMs', "Seconds", x => x / 1000);
}

function makeMarketingNames(agentNames, config) {
    return agentNames.map(agentName => {
        const agents = config.agents || [];
        const agent = agents.find( agent => agent.name === agentName);
        return agent ? agent.description : agentName;
    })
}

function makeChart(aggregated, config, resultType, axisTitle, scaleFunction = x => x) {
    const agentNames = aggregated['agents'];
    const descriptions = makeMarketingNames(agentNames, config);
    const initialResults = agentNames.map(agent => aggregated['results'][resultType][agent]);
    const results = initialResults.map(scaleFunction);
    new Chartist.Bar(`#${resultType}-chart`, {
            labels: descriptions,
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
                makeChartistAxisTitle(axisTitle)
            ]
        },
    );
}

function makeChartistAxisTitle(axisTitle) {
    return Chartist.plugins.ctAxisTitle({
        axisY: {
            axisTitle: axisTitle,
            axisClass: "ct-axis-title",
            offset: {
                x: 0,
                y: 15
            },
            flipTitle: true
        }
    });
}

function tiltLabels(){
    document.querySelectorAll('svg.ct-chart-bar, svg.ct-chart-line').forEach(x => {
        x.style.overflow = 'visible';
    })
    const labels = document.querySelectorAll('.ct-label.ct-label.ct-horizontal.ct-end');
    labels.forEach(label => {
        label.style.position = 'relative';
        label.style['justify-content'] = 'flex-end';
        label.style['text-align'] = 'right';
        label.style['transform-origin'] = '100% 0';
        label.style.transform = 'translate(-100%) rotate(-55deg)';
        label.style['white-space'] = 'nowrap';
        label.style['font-size'] = '0.6em';
    });
}

function straightLabels(){
    const labels = document.querySelectorAll('.ct-label.ct-label.ct-horizontal.ct-end');
    labels.forEach(label => {
        label.style.removeProperty('position');
        label.style.removeProperty('justify-content');
        label.style.removeProperty('text-align');
        label.style.removeProperty('transform-origin');
        label.style.removeProperty('transform');
        label.style.removeProperty('white-space');
    });
}
