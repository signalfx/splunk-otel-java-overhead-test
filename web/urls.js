// Tools for helping manage urls (for permalinking)

function getResultIdFromUrl(){
    const url = new URL(window.location.href);
    return url.searchParams.get('r');
}

function updateUrl(resultId){
    const currentUrl = new URL(window.location.href);
    currentUrl.searchParams.set("r", resultId);
    history.replaceState({}, '', currentUrl.toString());
}

function updateUrlForHistorical(){
    const url = new URL(window.location.href);
    url.searchParams.set('r', 'historical');
    history.replaceState({}, '', url.toString());
}

function urlIsShowingHistorical(){
    const url = new URL(window.location.href);
    return 'historical' === url.searchParams.get('r');
}