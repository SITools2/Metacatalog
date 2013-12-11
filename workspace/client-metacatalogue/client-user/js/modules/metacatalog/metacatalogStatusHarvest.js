/*******************************************************************************
 * Copyright 2010-2013 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 * 
 * This file is part of SITools2.
 * 
 * SITools2 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * SITools2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * SITools2. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/*global Ext, sitools, i18n, loadUrl */

Ext.namespace('sitools.user.modules');
/**
 * Help Module
 * @class sitools.user.modules.metacatalogStatusHarvest
 * @extends Ext.Panel
 */
sitools.user.modules.metacatalogStatusHarvest = Ext.extend(Ext.Panel, {
//    layout: 'fit',
    id : "metacatalogStatusHarvest",
    title : i18n.get('label.statusTitle'),
    initComponent : function () {
        
        this.store = new Ext.data.JsonStore({
            autoLoad : false,
            url : '/tmp',
            root : 'data',
            fields : [
            {
                name : 'id'
            },
            {
                name : 'nbDocumentsRetrieved',
                type : 'int'
            },{
                name : 'nbDocumentsIndexed',
                type : 'int'
            }, {
                name : 'nbDocumentsInvalid',
                type : 'int'
            }, {
                name : 'status',
                type : 'string'
            }, {
                name : 'errorCause',
                type : 'string'
            }, {
                name : 'result',
                type : 'string'
            }, {
                name : 'startDate',
                type : 'date',
                dateFormat : SITOOLS_DATE_FORMAT
            }, {
                name : 'endDate',
                type : 'date',
                dateFormat : SITOOLS_DATE_FORMAT
            }, {
                name : 'loggerFileName',
                type : 'string'
            }],
            listeners : {
                load : function (store, records, ind){
                        Ext.each(records, function (item){
                            if (!Ext.isDefined(item.startDate)){
                                item.startDate = "";
                            }
                            if (!Ext.isDefined(item.endDate)){
                                item.endDate = "";
                            }
                        });
                    }
            }
        });
        
        this.tpl = new Ext.XTemplate(
            '<tpl for=".">' +
                    '<tpl if="xindex == 1">' +
                    '<div id="currentHarvest" class="statusHarvestDetail">' +
                        '<span class="harvestTitle">Current Harvest</span>' +
                        '<a href="#" title="Refresh" onClick=\'sitools.user.modules.metacatalogCrud.onRefreshStatusDetail(); return false;\'>' +
                            '<img alt="ACTIVE" style=\'position:absolute; padding-left:4px\' src="/sitools/common/res/images/icons/refresh.png" />' +
                        '</a>' +
                        '<br>' +
                        '<p>' +
                        '<tpl if="status == \'ACTIVE\'">' +
                            'Status : <b>ACTIVE</b> <img alt="ACTIVE" style=\'position:absolute;\' src="/sitools/client-user/js/modules/metacatalog/images/active_status.gif" /> <br>' +
                        '</tpl>' +
                        '<tpl if="status==\'INACTIVE\'">' +
                            'Status : <b>INACTIVE</b> <img alt="INACTIVE" style=\'position:absolute;\' src="/sitools/client-user/js/modules/metacatalog/images/inactive_status.png" /> <br>' +
                        '</tpl>' +
                        '<br>' +
                    '</tpl>' +
                    '<tpl if="xindex == 2">' +
                    '<div id="lastHarvest" class="statusHarvestDetail">' +
                        '<span class="harvestTitle">Last Harvest</span>' +
                        '<br>' +
                        '<p>' +
                        '<span class="result-{result}"> Result : <b>{result}</b></span><br>' +
                        '<br>' +
                    '</tpl>' +
                        
                        'Documents Retrieved : <b>{nbDocumentsRetrieved}</b> <br>' +
                        'Documents Indexed : <b>{nbDocumentsIndexed}</b> <br>' +
                        'Documents Invalid : <b>{nbDocumentsInvalid}</b> <br>' +
                        '<tpl if="this.isEmpty(startDate) == false" >' +
                            'Start Date : <b>{startDate:date("'+SITOOLS_DEFAULT_IHM_DATE_FORMAT+'")}</b> <br>' +
                        '</tpl>' +
                        '<tpl if="this.isEmpty(endDate) == false" >' +
                            'End Date : <b>{endDate:date("'+SITOOLS_DEFAULT_IHM_DATE_FORMAT+'")}</b> <br>' +
                        '</tpl>' +
                        '<tpl if="this.isEmpty(errorCause) == false" >' +
                            '<span class="errorCause"> errorCause : ' +
                                '<a href="#" title="Show Error" onClick=\'sitools.user.modules.metacatalogStatusHarvest.showError("{errorCause}"); return false;\'>' +
                                    '<img alt="ACTIVE" style=\'position:absolute; padding-left:4px\' src="/sitools/client-user/js/modules/metacatalog/images/error.png" />' +
                                '</a>' +
                            '</span>' +
                            '<br>' +
                        '</tpl>' +
                        '<tpl if="this.isEmpty(loggerFileName) == false" >' +
                        '<span class="showLog">' +
                            '<a href="#" onClick=\'sitools.user.modules.metacatalogStatusHarvest.showLog("{loggerFileName}"); return false;\'>' +
                                'Show log...' +
                            '</a>' +
                        '</span>'+
                        '</tpl>' +
                    '</p>' +
                '</div>' +
            '</tpl>',
            {
                    compiled : true, 
                    scope : this,
                    isEmpty : function (item) {
                        return Ext.isEmpty(item);
                    }
            }
        );
        
        this.dataview = new Ext.DataView({
            store : this.store,
            tpl : this.tpl
        });
        
        this.items = [ this.dataview ];
        this.addListener("refreshHarvestStatus", this.refreshHarvestStatus);
        
        sitools.user.modules.metacatalogStatusHarvest.superclass.initComponent.call(this);
    },
    
    onRender : function () {
        sitools.user.modules.metacatalogStatusHarvest.superclass.onRender.apply(this, arguments);
    },
    
    /**
     * 
     * @param {} url the url to load status
     * @param {} crud metacatalogCrud
     */
    refreshHarvestStatus : function (url, id, crud) {
        this.url = url;
        this.store.proxy.conn.url = this.url;
        this.store.load();
        
        this.setTitle(i18n.get('label.statusTitle') + id);
        
        crud.store.load();
    }
});

sitools.user.modules.metacatalogStatusHarvest.showError = function(errorMessage) {
    new Ext.Window({
        title : i18n.get('label.errorCause'),
        html : errorMessage,
        autoHeight : true,
        width : 500
    }).show();
}

sitools.user.modules.metacatalogStatusHarvest.showLog = function(logName) {
    var statusHarvestPanel = Ext.getCmp('metacatalogStatusHarvest');
    new Ext.Window({
        title : 'Log',
        autoLoad : statusHarvestPanel.appUrl + "/logs/" + logName+"?_dc=" + new Date().getTime(),
        autoScroll : true,
        height : 400,
        width : 500,
        style : {
            "white-space" : 'pre-line'        
        }
    }).show();
}

Ext.reg('sitools.user.modules.metacatalogStatusHarvest', sitools.user.modules.metacatalogStatusHarvest);
