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
 * @class sitools.user.modules.metacatalogDataview
 * @extends Ext.Panel
 */
sitools.user.modules.metacatalogDataview = Ext.extend(Ext.Panel, {
	border : false,
    layout:'fit',
    id : "metacatalogDataview",
	initComponent : function () {
		this.title = i18n.get('label.generalInformation');
    	
    	this.store = new Ext.data.JsonStore({
            url : this.appUrl,
            fields : [
            {
                name : 'description',
                type : 'string'
            },{
            	name : 'contact',
                type : 'string'
            }, {
            	name : 'urlHarvester',
                type : 'string'
            }, {
            	name : 'urlCoreMetacatalogue',
                type : 'string'
            }, {
            	name : 'pendingOperation',
                type : 'boolean'
            }, {
            	name : 'pendingOperationMessage',
                type : 'string'
            }],
            listeners : {
            	add : function (store, records, ind){
            			if (!Ext.isDefined(records[0].data.pendingOperation)){
            				records[0].data.pendingOperation = false;
            			}
                    }
            }
        });
        
        this.tpl = new Ext.XTemplate(
        	'<tpl for=".">' +
		    	'<div class="catalog-core">' +
			    	'<div class="icon-catalog">' +
			    		'<img alt="Data Center" src="/sitools/client-user/js/modules/metacatalog/images/datacenter.png" />' +
			    	'</div>' +
			    	'<div class="details-catalog">' +
		    			'<p>{description}</p>' +
		    			'<p><b>Contact :</b> <a href="mailto:{contact}">{contact}</a></p>' +
		    			'<p><b>Url Harvester :</b> <a href="{urlHarvester}" target="_blank">{urlHarvester}</a></p>' +
		    			'<p><b>Url Core :</b> <a href="{urlCoreMetacatalogue}" target="_blank">{urlCoreMetacatalogue}</a></p>' +
		    			'<tpl if="pendingOperation == true">' +
		    				'<p><b>Core Status :</b> <span style="color:red; font-weight:bold;">Operation Pending</span>' +
                            ' -- {pendingOperationMessage}</p>' +
		    			'</tpl>' +
		    			'<tpl if="pendingOperation == false">' +
		    				'<p><b>Core Status :</b> <span style="color:green; font-weight:bold;">No Operation in progress</span></p>' +
		    			'</tpl>' +
                        '<p></p>' +
                        '<p>'+
                            '<a href="#" onClick=\'sitools.user.modules.metacatalogDataview.merge("{this.scope.appUrl}"); return false;\'>' +
                                '{[this.i18nTpl("label.mergeIndexes")]}' +
                             '</a>' +
                         '</p>' +
                        '<p></p>' +
                        '<p>'+
                            '<a href="#" onClick=\'sitools.user.modules.metacatalogDataview.refreshStatus(); return false;\'>' +
                                '{[this.i18nTpl("label.refresh")]}' +
                             '</a>' +
                         '</p>' +
			    	'</div>' +
		    	'</div>' +
		    '</tpl>',
			{
					compiled : true, 
					scope : this,
					i18nTpl : function (label){
						return i18n.get(label);
					}
			}
		);
        
    	this.dataview = new Ext.DataView({
    		layout : 'fit',
    		store : this.store,
    		tpl : this.tpl,
    		autoHeight:true
    	});
		
    	this.items = [ this.dataview ];
    	
	    this.addListener("refreshMetacatalogStatus", this.refreshMetacatalogStatus);
        
		this.bodyCssClass = 'metaView-panel-background';
		
	    sitools.user.modules.metacatalogDataview.superclass.initComponent.call(this);
    },
    
    onRender : function () {
        sitools.user.modules.metacatalogDataview.superclass.onRender.apply(this, arguments);
        this.refreshMetacatalogStatus();
        
    },
    
    refreshMetacatalogStatus : function () {
        Ext.Ajax.request({
            url : this.appUrl,
            method : 'GET',
            scope : this,
            success : function (response, opts) {
                var json = Ext.decode(response.responseText);
                if (json.success) {
                    var rec = new Ext.data.Record(json.metacatalogStatus);
                    this.store.removeAll();
                    this.store.add(rec);
                } 
            },
            failure : alertFailure
        });
    }
    

});

sitools.user.modules.metacatalogDataview.merge = function (url){
     var notify = new Ext.ux.Notification({
        iconCls : 'x-icon-information',
        title : i18n.get('label.information'),
        html : i18n.get("label.metacatalogue.merge.start"),
        autoDestroy : true,
        hideDelay : 1000
    });
    notify.show(document);
    
    Ext.Ajax.request({
        url : url + "/merge",
        method : 'PUT',
        scope : this,
        success : function (ret) {
            if (showResponse(ret)) {
                var notify = new Ext.ux.Notification({
                    iconCls : 'x-icon-information',
                    title : i18n.get('label.information'),
                    html : i18n.get('metacatalog.merged.succeed'),
                    autoDestroy : true,
                    hideDelay : 1000
                });
                notify.show(document);   
            }
        },
        callback : function(){
                sitools.user.modules.metacatalogDataview.refreshStatus();
        },
        failure : alertFailure
    });
}

sitools.user.modules.metacatalogDataview.refreshStatus = function (){
    var metacatalogDataView = Ext.getCmp("metacatalogDataview");
    metacatalogDataView.fireEvent("refreshMetacatalogStatus");    
}

Ext.reg('sitools.user.modules.metacatalogDataview', sitools.user.modules.metacatalogDataview);
