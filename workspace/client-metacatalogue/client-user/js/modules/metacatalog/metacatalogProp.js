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
 * @class sitools.user.modules.metacatalogProp
 * @extends Ext.Panel
 */
sitools.user.modules.metacatalogProp = Ext.extend(Ext.Window, {
	width : 450,
    modal : true,
	id : 'metaPropWinId',
	padding : "10px 10px 10px 10px",
    
    initComponent : function () {
    	
	    var storeCatalogType = new Ext.data.JsonStore({
	    	proxy : new Ext.data.HttpProxy({
                method : 'GET',
                url: this.appUrl + '/ihm/catalogsTypes'
            }),
            idProperty : 'catalogType',
            fields : ['catalogType']
		});
		
		var storeCatalogClassName = new Ext.data.JsonStore({
	    	proxy : new Ext.data.HttpProxy({
                method : 'GET',
                url: this.appUrl + '/ihm/havestersClasses'
            }),
            idProperty : 'harvesterClassName',
            fields : ['harvesterClassName']
		});
    	
    	var metaStoreFields = new Ext.data.JsonStore({
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
            }/*, {
                name : 'lastHarvest',
                type : 'date',
                dateFormat: 'timestamp'
            }*/, {
                name : 'attributes'
            }, {
            	name : 'automaticMerge',
            	type : 'boolean'
            }, {
                name : 'publicServices',
                type : 'boolean'
            }],
            autoLoad : false
        });
        
        var metaAttributesStoreFields = new Ext.data.JsonStore({
            fields : [{
                name : 'name',
                type : 'string'
            }, {
                name : 'value',
                type : 'string'
            }, {
                name : 'path',
                type : 'string'
            }]
        });
        
        var smMetaFields = new Ext.grid.RowSelectionModel({
            singleSelect : true
        });

        var cmMetaFields = new Ext.grid.ColumnModel({
            columns : [ {
                header : i18n.get('headers.key'),
                dataIndex : 'name',
                editor : new Ext.form.TextField()
            }, {
                header : i18n.get('headers.value'),
                dataIndex : 'value',
                editor : new Ext.form.TextField()
            }, {
                header : i18n.get('headers.path'),
                dataIndex : 'path',
                editor : new Ext.form.TextField()
            }],
            defaults : {
                sortable : false,
                width : 100
            }
        });

        var metaGridTBar = {
            defaults : {
                scope : this
            },
            items : [ {
                text : i18n.get('label.create'),
                icon : loadUrl.get('APP_URL') + '/common/res/images/icons/toolbar_create.png',
                handler : this.onCreateProperties
            }, {
                text : i18n.get('label.delete'),
                icon : loadUrl.get('APP_URL') + '/common/res/images/icons/toolbar_delete.png',
                handler : this.onDeleteProperties
            }]
        };
        
        this.metaGridProperties = new Ext.grid.EditorGridPanel({
            title : i18n.get('title.additionalMetaFields'),
            id : 'metaGridFields',
            tbar : metaGridTBar,
            height : 200,
            autoScroll : true,
            columnLines : true,
            bodyCssClass : 'background-color:red;',
            clicksToEdit : 2,
            store : metaAttributesStoreFields,
            cm : cmMetaFields,
            sm : smMetaFields,
            viewConfig : {
                forceFit : true,
                getRowClass : function (row, col) { 
                } 
            },
            listeners : {
                beforeedit : function (e) {
                }
            }
        });
        
		this.items = [{
            xtype : 'form',
            buttonAlign : 'center',
            id : 'metacatalogForm',
            labelWidth : 150,
            items : [{
                xtype : 'textfield',
                fieldLabel : i18n.get('label.id'),
                name : 'id',
                id : 'metaId',
                allowBlank : false,
                anchor : '100%'
            }, {
                xtype : 'combo',
                fieldLabel : i18n.get('label.catalogType'),
                name : 'catalogType',
                id : 'catalogTypeId',
                allowBlank : false,
                anchor : '100%',
                typeAhead: true,
			    triggerAction: 'all',
			    lazyRender:true,
			    store : storeCatalogType,
			    valueField: 'catalogType',
    			displayField: 'catalogType'
            }, {
                xtype : 'textfield',
                fieldLabel : i18n.get('label.description'),
                name : 'description',
                id : 'descriptionId',
                allowBlank : false,
                anchor : '100%'
            }, {
                xtype : 'combo',
                fieldLabel : i18n.get('label.className'),
                name : 'harvesterClassName',
                id : 'classNameId',
                allowBlank : false,
                anchor : '100%',
                typeAhead: true,
			    triggerAction: 'all',
			    lazyRender:true,
			    store : storeCatalogClassName,
			    valueField: 'harvesterClassName',
    			displayField: 'harvesterClassName'
            }, {
                xtype : 'textfield',
                fieldLabel : i18n.get('label.indexerUrl'),
                name : 'indexerUrl',
                id : 'indexerUrl',
                allowBlank : false,
                anchor : '100%'
            }, {
                xtype : 'textfield',
                fieldLabel : i18n.get('label.sourceUrl'),
                name : 'sourceUrl',
                id : 'sourceUrl',
                allowBlank : false,
                anchor : '100%'
            }/*, {
                xtype : 'textfield',
                name : 'lastHarvest',
                id : 'lastHarvest',
                hidden : true
            }*/, {
                xtype : 'checkbox',
                fieldLabel : i18n.get('label.automaticMerge'),
                name : 'automaticMerge',
                id : 'automaticMergeId',
                anchor : '100%'
            }, {
                xtype : 'checkbox',
                fieldLabel : i18n.get('label.publicServices'),
                name : 'publicServices',
                id : 'publicServicesId',
                anchor : '100%'
            }, this.metaGridProperties],
            buttons : ['->',{
                text : (this.action == 'create') ? i18n.get('label.create') : i18n.get('label.modify'),
                handler : this.onValidate,
                scope : this
            },{
                text : i18n.get('label.cancel'),
                scope : this,
                handler : function (){
                	this.close();
                }
            }]
        }];
    	
	    sitools.user.modules.metacatalogProp.superclass.initComponent.call(this);
    },
    
    /**
     * done a specific render to load metacatalog modules properties. 
     */
    onRender : function () {
        sitools.user.modules.metacatalogProp.superclass.onRender.apply(this, arguments);
        if (this.action == 'modify') {
            var f = this.findByType('form')[0].getForm();
            Ext.Ajax.request({
                url : this.url,
                method : 'GET',
                scope : this,
                success : function (ret) {
                    var data = Ext.decode(ret.responseText);
                    f.setValues(data.harvesterModel);
                    f.findField('indexerUrl').setValue(data.harvesterModel.indexerConf.url);
                    f.findField('sourceUrl').setValue(data.harvesterModel.source.url);
//                    f.findField('lastHarvest').setValue(data.harvesterModel.lastHarvest);
                    
                    var attributes = data.harvesterModel.attributes;
                    var storeMetaProperties = this.metaGridProperties.getStore();
                    
                    if (!Ext.isEmpty(attributes)) {
                        Ext.each(attributes, function (item) {
                            storeMetaProperties.add(new Ext.data.Record(item));
                        }, this);
                    }
                },
                failure : alertFailure
            });
        }
    },
    
    onValidate : function (){
    	var frm = this.findByType('form')[0].getForm();
    	
        if (!frm.isValid()) {
            Ext.Msg.alert(i18n.get('label.warning'), i18n.get('warning.invalidForm'));
            return;
        }
        var method = this.action == 'modify' ? 'PUT' : 'POST';
        
        var jsonObject = {};
        
        Ext.iterate(frm.getFieldValues(), function (key, value) {
            if (key == 'indexerUrl') {
                jsonObject.indexerConf = {};
                jsonObject.indexerConf.url = value;
            } else if (key == 'sourceUrl'){
                jsonObject.source = {};
                jsonObject.source.url = value;        	
        	}
            else {
                jsonObject[key] = value;
            }
        }, this);
        
        jsonObject.attributes = [];
        
        this.metaGridProperties.getStore().each(function (item) {
            jsonObject.attributes.push({
                name : item.data.name,
                value : item.data.value,
                path : item.data.path
            });
        });
        
        Ext.Ajax.request({
            url : this.url,
            method : method,
            jsonData : jsonObject,
            scope : this,
            success : function (response, opts) {
                var json = Ext.decode(response.responseText);
                if (json.success) {
                    var notify = new Ext.ux.Notification({
                        iconCls : 'x-icon-information',
                        title : i18n.get('label.information'),
                        html : i18n.get('label.metacatalogSaved'),
                        autoDestroy : true,
                        hideDelay : 1000
                    });
                    notify.show(document);
                    
                    this.store.reload();
                	this.close();
                } 
            },
            failure : function (response, opts) {
                var txt;
                if (response.status == 200) {
                    var ret = Ext.decode(response.responseText).message;
                    txt = i18n.get('msg.error') + ': ' + ret;
                } else {
                    txt = i18n.get('warning.serverError') + ': ' + response.statusText;
                }
            }
        });
        
    },
    
    /**
     * Add a new Record to the dependencies property of a project module
     */
    onCreateProperties : function () {
        var e = new Ext.data.Record();
        var rowNbr = this.metaGridProperties.getStore().getCount();
        this.metaGridProperties.getStore().insert(rowNbr, e);
        
        var row = this.metaGridProperties.getView().getRow(rowNbr);
        var elRow = Ext.get(row);
        elRow.frame("#97EB6E", 1);
        this.metaGridProperties.startEditing(rowNbr, 0);
    },
    
    /**
     * Delete the selected dependency of a project module
     */
    onDeleteProperties : function () {
        var s = this.metaGridProperties.getSelectionModel().getSelections();
        var i, r;
        for (i = 0; s[i]; i++) {
            r = s[i];
            this.metaGridProperties.getStore().remove(r);
        }
    },

    /**
     * method called when trying to save preference
     * @returns
     */
    _getSettings : function () {
		return {
            preferencesPath : "/modules", 
            preferencesFileName : this.id
        };

    }
    
});

Ext.reg('sitools.user.modules.metacatalogProp', sitools.user.modules.metacatalogProp);
