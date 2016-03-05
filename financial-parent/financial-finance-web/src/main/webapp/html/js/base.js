/**
 * Created with JetBrains WebStorm.
 * User: wangruowen
 * Date: 15-8-5
 * Time: 下午4:14
 * To change this template use File | Settings | File Templates.
 */
//REM自适配
(function (doc, win) {
    var docEl = doc.documentElement,
        resizeEvt = 'orientationchange' in window ? 'orientationchange' : 'resize',
        recalc = function () {
            var clientWidth = docEl.clientWidth;
            if (!clientWidth) return;
            if(clientWidth > 640){
                //clientWidth = 640;
            }
            docEl.style.fontSize = 20 * (clientWidth / 320) + 'px';
        };

    if (!doc.addEventListener) return;
    win.addEventListener(resizeEvt, recalc, false);
    doc.addEventListener('DOMContentLoaded', recalc, false);
})(document, window);

