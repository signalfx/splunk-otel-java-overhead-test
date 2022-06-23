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

