
// Returns a list of available historic runs
async function getRuns(){
    return fetch(`results/index.txt`)
        .then(resp => resp.text())
        .then(body =>
            body.split("\n")
                .filter(x => x.match(/^\d{8}_/))
        );
}

async function getResults(name){
    return fetch(`results/${name}/results.csv`)
        .then(resp => resp.text())
        .then(body => parseCsv(body))
        .then(data => {
            // console.log(data);
            const aggregated = aggregateRunData(data);
            console.log(aggregated)
            return aggregated;
        });
}

async function getConfig(name){
    const path = `results/${name}/config.json`;
    return fetch(path)
        .then(resp => resp.json())
        .catch(e => {
            console.log(`No such config found for ${path}`)
            return {};
    });
}