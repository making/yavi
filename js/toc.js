var toctitle = document.getElementById('toctitle');
var path = window.location.pathname;
if (toctitle != null) {
    var oldtoc = toctitle.nextElementSibling;
    var newtoc = document.createElement('div');
    newtoc.setAttribute('id', 'tocbot');
    newtoc.setAttribute('class', 'js-toc desktop-toc');
    oldtoc.setAttribute('class', 'mobile-toc');
    oldtoc.parentNode.appendChild(newtoc);
    tocbot.init({
        contentSelector: '#content',
        headingSelector: 'h1, h2, h3, h4, h5',
        positionFixedSelector: 'body',
        fixedSidebarOffset: 90,
        smoothScroll: false
    });
    if (!path.endsWith("index.html") && !path.endsWith("/")) {
        var link = document.createElement("a");
        if (document.getElementById('index-link')) {
            indexLinkElement = document.querySelector('#index-link > p > a');
            linkHref = indexLinkElement.getAttribute("href");
            link.setAttribute("href", linkHref);
        } else {
            link.setAttribute("href", "index.html");
        }
        link.innerHTML = "<span><i class=\"fa fa-chevron-left\" aria-hidden=\"true\"></i></span> Back to index";
        var block = document.createElement("div");
        block.setAttribute('class', 'back-action');
        block.appendChild(link);
        var toc = document.getElementById('toc');
        var next = document.getElementById('toctitle').nextElementSibling;
        toc.insertBefore(block, next);
    }
}

var headerHtml = '<div id="header-spring">\n' +
    '<h1>\n' +
    '<p><img src="https://user-images.githubusercontent.com/106908/120071055-66770380-c0c8-11eb-83f1-d7eff04bad54.png" alt="YAVI" style="height: 90px"></p>' +
    '\n' +
    '</h1>\n' +
    '</div>';

var header = document.createElement("div");
header.innerHTML = headerHtml;
document.body.insertBefore(header, document.body.firstChild);


if (window.matchMedia('screen and (max-width: 768px)').matches) {
    document.getElementById("toc").remove();
    document.body.className = "book";
}