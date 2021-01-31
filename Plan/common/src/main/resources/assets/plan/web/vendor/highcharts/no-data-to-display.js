/*
 Highcharts JS v8.2.2 (2020-10-22)

 Plugin for displaying a message when there is no data visible in chart.

 (c) 2010-2019 Highsoft AS
 Author: Oystein Moseng

 License: www.highcharts.com/license
*/
(function(a){"object"===typeof module&&module.exports?(a["default"]=a,module.exports=a):"function"===typeof define&&define.amd?define("highcharts/modules/no-data-to-display",["highcharts"],function(d){a(d);a.Highcharts=d;return a}):a("undefined"!==typeof Highcharts?Highcharts:void 0)})(function(a){function d(a,c,d,e){a.hasOwnProperty(c)||(a[c]=e.apply(null,d))}a=a?a._modules:{};d(a,"Extensions/NoDataToDisplay.js",[a["Core/Chart/Chart.js"],a["Core/Utilities.js"]],function(a,c){var d=c.addEvent,e=c.extend,
f=c.getOptions;c=a.prototype;f=f();e(f.lang,{noData:"No data to display"});f.noData={attr:{zIndex:1},position:{x:0,y:0,align:"center",verticalAlign:"middle"},style:{fontWeight:"bold",fontSize:"12px",color:"#666666"}};c.showNoData=function(a){var b=this.options;a=a||b&&b.lang.noData;b=b&&(b.noData||{});this.renderer&&(this.noDataLabel||(this.noDataLabel=this.renderer.label(a,0,0,void 0,void 0,void 0,b.useHTML,void 0,"no-data").add()),this.styledMode||this.noDataLabel.attr(b.attr).css(b.style||{}),
this.noDataLabel.align(e(this.noDataLabel.getBBox(),b.position||{}),!1,"plotBox"))};c.hideNoData=function(){this.noDataLabel&&(this.noDataLabel=this.noDataLabel.destroy())};c.hasData=function(){for(var a=this.series||[],b=a.length;b--;)if(a[b].hasData()&&!a[b].options.isInternal)return!0;return this.loadingShown};d(a,"render",function(){this.hasData()?this.hideNoData():this.showNoData()})});d(a,"masters/modules/no-data-to-display.src.js",[],function(){})});
//# sourceMappingURL=no-data-to-display.js.map