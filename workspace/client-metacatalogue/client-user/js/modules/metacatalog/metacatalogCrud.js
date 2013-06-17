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
/* global Ext, sitools, i18n, loadUrl */

Ext.namespace('sitools.user.modules');
/**
 * Help Module
 * 
 * @class sitools.user.modules.metacatalogCrud
 * @extends Ext.Panel
 */
sitools.user.modules.metacatalogCrud = Ext.extend(Ext.grid.GridPanel, {
	border : false,
	sm : new Ext.grid.RowSelectionModel(),
    id : "metacatalogCrud",
    pageSize : 10,
	
	initComponent : function () {
    	this.url = this.appUrl + '/admin';
    	
    	this.store = new Ext.data.JsonStore({
            root : 'data',
            restful : true,
            remoteSort : true,
            autoSave : false,
            url : this.url,
            idProperty : 'id',
            listeners : {
            	scope : this,
            	load : function (store, records, optns){
            		Ext.each(records, function (item, ind, all){
				    		if (item.data.status == "ACTIVE"){
					    		var id = item.id;
				    			this.lockActions(id, 'label.harvesting');
				    		}
				    	}, this);
            	}
            },
            fields : [{
                name : 'id',
                type : 'string'
            }, {
                name : 'catalogType',
                type : 'string'
            }, {
                name : 'description',
                type : 'string'
            }, {
                name : 'source'
            }, {
                name : 'harvesterClassName',
                type : 'string'
            }, {
                name : 'properties'
            }, {
                name : 'indexerConf'
            }, {
                name : 'lastHarvest',
                type : 'date'
            }, {
                name : 'attributes'
            }, {
            	name : 'automaticMerge',
            	type : 'boolean'
            }, {
                name : 'status'
            }]
        });
        
        var expander = new Ext.ux.grid.RowExpander({
	        tpl : new Ext.XTemplate(
	        	'<tpl if="this.descEmpty(description) == false" >' +
					'<div class="sitoolsDescription">' +
						'<div class="sitoolsDescriptionHeader">Description :&nbsp;</div>' +
						'<span class="sitoolsDescriptionText"> {description} </span>' +
						'<div class="sitoolsDescriptionHeader">Class Name :&nbsp;</div>' +
						'<span class="sitoolsDescriptionText"> {harvesterClassName} </span>' +
	                '</div>' +
             	'</tpl>',
             	'<tpl if="this.descEmpty(description) == true" >' +
             	'<div class="sitoolsDescription">' +
						'<div class="sitoolsDescriptionHeader">Class Name :&nbsp;</div>' +
						'<span class="sitoolsDescriptionText"> {harvesterClassName} </span>' +
	                '</div>' +
             	'</tpl>',
             	{
					compiled : true, 
					scope : this,
					descEmpty : function (description) {
						return Ext.isEmpty(description);
					}
				}
           )
	    });
    	
    	this.cm = new Ext.grid.ColumnModel({
            defaults : {
                sortable : true
            },
            columns : [expander,{
                header : i18n.get('label.id'),
                dataIndex : 'id',
                width : 100,
                sortable : true
            }, {
                header : i18n.get('label.catalogType'),
                dataIndex : 'catalogType',
                width : 100,
                sortable : true
            }, {
            	xtype: 'datecolumn',
                header : i18n.get('label.lastHarvest'),
                dataIndex : 'lastHarvest',
                format : SITOOLS_DEFAULT_IHM_DATE_FORMAT,
                width : 250,
                sortable : true
            }, {
            	xtype : 'actioncolumn',
            	width : 110,
            	align : 'center', 
            	header : i18n.get('label.catalogAction'),
            	items : [{
                    icon   : '/sitools/client-user/js/modules/metacatalog/images/toolbar_active.png',
                    iconCls : 'formatIconsAction',
                    tooltip: i18n.get('label.startHarvert'),
                    handler: function(grid, rowIndex, colIndex) {
                        var rec = grid.store.getAt(rowIndex);
	                    grid.start(rec.id, grid.url);
                    }
                }, {
                    icon   : '/sitools/client-user/js/modules/metacatalog/images/clean-icon.png',
                    iconCls : 'formatIconsAction',
                    tooltip: i18n.get('label.cleanAndStart'),
                    handler: function(grid, rowIndex, colIndex) {
                        var rec = grid.store.getAt(rowIndex);
	                    grid.cleanAndStart(rec.id, grid.url);
                    }
                }, /*{
                    icon   : '/sitools/client-user/js/modules/metacatalog/images/toolbar_disactive.png',
                    iconCls : 'formatIconsAction',
                    tooltip: i18n.get('label.stopHarvest'),
                    handler: function(grid, rowIndex, colIndex) {
                        var rec = grid.store.getAt(rowIndex);
	                    grid.stop(rec.id, grid.url);
                    }
                }, */{
                    icon   : '/sitools/client-user/js/modules/metacatalog/images/toolbar_clean.png',
                    iconCls : 'formatIconsAction',
                    tooltip: i18n.get('label.cleanCatalog'),
                    handler: function(grid, rowIndex, colIndex) {
                        var rec = grid.store.getAt(rowIndex);
	                    grid.clean(rec.id, grid.url);
                    }
                }],
                scope : this,
                renderer : function (value, metadata, rec, rowIndex, colIndex, store){
					metadata.attr += "id=" + rec.id +" ";       
					value = String.format('<div id="{0}">', rec.id);
                	return value;
                }
            }]
        });
    	
		var addCatalogBtn = new Ext.Button({
			text : i18n.get('label.add'),
			icon : loadUrl.get('APP_URL') + '/common/res/images/icons/toolbar_create.png',
			scope : this,
			handler : this.onAdd,
	        xtype : 's-menuButton'
		});   
		
		var modifyCatalogBtn = new Ext.Button({
			text : i18n.get('label.modify'),
			icon : loadUrl.get('APP_URL') + '/common/res/images/icons/toolbar_edit.png',
			scope : this,
			handler : this.onModify,
	        xtype : 's-menuButton'
		});   
		
		var deleteCatalogBtn = new Ext.Button({
			text : i18n.get('label.delete'),
			icon : loadUrl.get('APP_URL') + '/common/res/images/icons/toolbar_delete.png',
			scope : this,
			handler : this.onDelete,
	        xtype : 's-menuButton'
		}); 
		
		this.tbar = {
	            xtype : 'toolbar',
	            defaults : {
	                scope : this
	            },
	            items : [ addCatalogBtn, modifyCatalogBtn, deleteCatalogBtn ]
	    };
	    
	    this.bbar = {
            xtype : 'paging',
            pageSize : this.pageSize,
            store : this.store,
            displayInfo : true,
            displayMsg : i18n.get('paging.display'),
            emptyMsg : i18n.get('paging.empty')
        };
	    
        this.view = new Ext.grid.GridView({
            forceFit : true
        });
        
         this.plugins = expander;
         this.addListener("refreshMetacatalogStore", this.refreshMetacatalogStore);
         
		this.on('rowclick',function(t, ind, e){
			
			// évite d'appeler le refresh quand on clic sur un icone (start, clean...)
			if (e.getTarget().className.match("formatIconsAction") ||
				e.getTarget().className.match("x-grid3-row-expander")) {
				return;
			}
			
			var row = this.getView().getRow(ind);
			e = Ext.get(row);
			this.onStatusDetail(e);
		});
		
	    sitools.user.modules.metacatalogCrud.superclass.initComponent.call(this);
    },
    
    onRender : function () {
        sitools.user.modules.metacatalogCrud.superclass.onRender.apply(this, arguments);
        this.store.load({
            params : {
                start : 0,
                limit : this.pageSize
            }
        });
    },
    
    refreshMetacatalogStore : function () {
        Ext.Ajax.request({
            url : this.url,
            method : 'GET',
            scope : this,
            success : function (response, opts) {
                var json = Ext.decode(response.responseText);
                if (json.success) {
                    this.store.loadData(json);
                } 
            },
            failure : alertFailure
        });
    },
    
    onAdd : function (){
    	var addMeta = new sitools.user.modules.metacatalogProp({
    		title : i18n.get('label.createCatalog'),
    		url : this.url,
    		appUrl : this.appUrl,
    		store : this.store,
            action : 'create'
        });
        addMeta.show();
    },
    
    onModify : function (){
    	var rec = this.getSelectionModel().getSelected();
    	
        if (!rec) {
            return Ext.Msg.alert(i18n.get('label.warning'), i18n.get('warning.noselection'));
        }

        var modifMeta = new sitools.user.modules.metacatalogProp({
        	title : i18n.get('label.modifyCatalog'),
            url : this.url + '/' + rec.id,
            appUrl : this.appUrl,
            action : 'modify',
            store : this.store
        });
        modifMeta.show();
    },
    
    onDelete : function (){
    	var rec = this.getSelectionModel().getSelected();
    	
    	if (!rec) {
    		return Ext.Msg.alert(i18n.get('label.warning'), i18n.get('warning.noselection'));
        }

        var tot = Ext.Msg.show({
            title : i18n.get('label.delete'),
            buttons : Ext.Msg.YESNO,
            msg : String.format(i18n.get('metacatalog.delete'), rec.data.id),
            scope : this,
            fn : function (btn, text) {
                if (btn == 'yes') {
                    this.doDelete(rec);
                }
            }

        });
    },
    
    onStatusDetail : function (e){
    	var rec = this.getSelectionModel().getSelected();
    	
    	if (!rec) {
    		return Ext.Msg.alert(i18n.get('label.warning'), i18n.get('warning.noselection'));
        }
        
        var url = this.url + '/' + rec.id + '/harvest/status';
        
        this.statusHarvest.refreshHarvestStatus(url, rec.id, this);
    },
    
    /**
	 * done the delete of the passed record
	 * 
	 * @param rec
	 *            the record to delete
	 */
    doDelete : function (rec) {
        Ext.Ajax.request({
            url : this.url + "/" + rec.id,
            method : 'DELETE',
            scope : this,
            success : function (ret) {
                if (showResponse(ret)) {
                    this.store.reload();
                }
            },
            failure : alertFailure
        });
    },
    
    lockActions: function (id, msg) {
    	if (Ext.get(id) == null){
    		id = Ext.id();
    	}
    	
		var pb = new Ext.ProgressBar({
			cls : 'progressBarMeta',
			id : id,
			renderTo : id
		});
		pb.wait({
	        interval:200,
	        increment:15,
	        text : i18n.get(msg)
		});	
		return pb;
	},
	
	/*
	unlockActions : function (pb) {
		pb.destroy();
	},
	*/
	start : function (id, url){
	    var callback = Ext.createDelegate(sitools.user.modules.metacatalogCrud.onRefreshStatusDetail, this, [this, id]);
	    this.handleCall(String.format("{0}/{1}/harvest/start", url, id), 'metacatalog.harvesting.started', id, 'label.harvesting', callback);
	},

	cleanAndStart : function (id, url){
	    var callback = Ext.createDelegate(sitools.user.modules.metacatalogCrud.onRefreshStatusDetail, this, [this, id]);
	    this.handleCall(String.format("{0}/{1}/harvest/cleanAndStart", url, id), 'metacatalog.harvesting.started', id, 'label.harvesting', callback);

	},

	stop : function (id, url){    
	    this.handleCall(String.format("{0}/{1}/harvest/stop", url, id), 'metacatalog.harvesting.stopped', id, 'label.stopping');	
	},
	
	clean : function (id, url){
		var callback = Ext.createDelegate(sitools.user.modules.metacatalogCrud.onRefreshStatusDetail, this, [this, id]);
	    this.handleCall(String.format("{0}/{1}/harvest/clean",url,id), 'metacatalog.cleaned', id, 'label.cleaning', callback);
	},

	handleCall : function (url, label, id, msg, callback){

		this.lockActions(id, msg);
	    Ext.Ajax.request({
	        url : url ,
	        method : 'PUT',
	        scope : this,
	        success : function (response) {
	            var json = Ext.decode(response.responseText);
	            if (json.success && !Ext.isEmpty(json.harvestResult)) {
	                var text = response.responseText;
	                if (Ext.isFunction(JSON.parse) && Ext.isFunction(JSON.stringify)) {
	                    text = JSON.stringify(json, null, 4);
	                    text = "<span style='white-space: pre'>"+text+"</span>";
	                }
	                Ext.Msg.alert(i18n.get('label.info'), text);
	            }
	            else if (showResponse(response)) {
	                var notify = new Ext.ux.Notification({
	                    iconCls : 'x-icon-information',
	                    title : i18n.get('label.information'),
	                    html : i18n.get(label),
	                    autoDestroy : true,
	                    hideDelay : 1000
	                });
	                notify.show(document);
	            }
	        },
	        failure : this.alertFailure,
	        callback : callback
	    });
	},

	alertFailure : function (response, opts) {
	    var txt;
	    if (response.isAbort && response.isTimeout) {
	    	 txt = i18n.get('warning.serverError') + ': ' + "Operation pending, check the metacatalog informations for more details";
	    } else if (response.status == SERVER_OK) {
	        var ret = Ext.decode(response.responseText).message;
	        txt = i18n.get('msg.error') + ': ' + ret;
	    } else if (response.status == 409) {
	        txt = i18n.get('warning.serverError') + ': ' + "Another operation is pending, check the metacatalog informations for more details";
	    } else {
	        txt = i18n.get('warning.serverError') + ': ' + response.statusText;
	    }
	    Ext.Msg.alert(i18n.get('label.warning'), txt);
	}
	
});

sitools.user.modules.metacatalogCrud.onRefreshStatusDetail = function (crud, id){
		if (!Ext.isDefined(crud)){
			var crud = Ext.getCmp("metacatalogCrud");
		}
	
        if (!Ext.isDefined(id)){
	    	var rec = crud.getSelectionModel().getSelected();
    		if (!rec) {
    			return Ext.Msg.alert(i18n.get('label.warning'), i18n.get('warning.noselection'));
        	}
	        id = rec.id;
        }
	
		var recFromId = crud.store.getById(id);
		if (Ext.isDefined(recFromId)){
			crud.getSelectionModel().selectRecords([recFromId]);
		}
    	
        var url = crud.url + '/' + id + '/harvest/status';
        crud.statusHarvest.refreshHarvestStatus(url, id, crud);
},

Ext.reg('sitools.user.modules.metacatalogCrud', sitools.user.modules.metacatalogCrud);
