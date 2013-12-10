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
 * @class sitools.user.modules.mainMetacatalogModule
 * @extends Ext.Panel
 */
sitools.user.modules.mainMetacatalogModule = Ext.extend(Ext.Panel, {

	initComponent : function () {
		
	    (Ext.isEmpty(userLogin)) ? this.user = "public" : this.user = userLogin;
	    
		Ext.each(this.listProjectModulesConfig, function (config){
            switch (config.name){
            case "urlMetacatalogApp" :
                this.appUrl = config.value;
                break;
            }
        }, this);
		
		this.metacatalogCrudTab = new sitools.user.modules.metacatalogCrud({
			flex : 1,
			appUrl : this.appUrl
		});
		
		this.metacatalogDataview = new sitools.user.modules.metacatalogDataview({
			appUrl : this.appUrl
		});
		
		this.metacatalogStatusHarvest = new sitools.user.modules.metacatalogStatusHarvest({
			height : 220,
			appUrl : this.appUrl,
			bodyCssClass : 'statusDetailsView-panel-background'
		});
		
		this.metacatalogCrudTab.statusHarvest = this.metacatalogStatusHarvest;
		
		this.metacatalogGrid = new Ext.Panel({
			title : i18n.get('label.listCatalog'),
			layout : {
				type : 'vbox',
				align : 'stretch',
				pack : 'start'
			},
			items : [this.metacatalogCrudTab, this.metacatalogStatusHarvest]
		});
		
		this.tabPanel = new Ext.TabPanel({
			activeTab : 0,
			items : [ this.metacatalogDataview, this.metacatalogGrid ],
            listeners : {
                scope : this,
                beforetabchange : this.beforeTabChange
            }
		});
		
		// Add a toolbar with a warning message
	    if (Ext.isEmpty(userLogin)) {
            this.tbar = new Ext.Toolbar({
                cls : 'services-toolbar',
                name : 'warningDateLabel',
                html : "<span style='font-style:italic;'><img src='/sitools/common/res/images/ux/warning.gif'/> " + i18n.get('warning.warnPublicMetacatalog') + "</span>"
            });
        }
		
		
		this.items = [this.tabPanel];
		
	    sitools.user.modules.mainMetacatalogModule.superclass.initComponent.call(this);
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
    },
    
    beforeTabChange : function (self, newTab, currentTab) {
        if (newTab.id == "metacatalogDataview") {
            newTab.fireEvent("refreshMetacatalogStatus");
        } else if (newTab.id == "metacatalogCrud") {
            newTab.fireEvent("refreshMetacatalogStore");
        }

    }

});


sitools.user.modules.mainMetacatalogModule.getParameters = function () {
    
    return [{
        jsObj : "Ext.form.TextField", 
        config : {
            fieldLabel : i18n.get("label.urlMetacatalogApp"),
            allowBlank : false,
            width : 150,
            listeners: {
                render: function(c) {
                  Ext.QuickTips.register({
                    target: c,
                    text: "the URL configured in the Application plugin"
                  });
                }
            },
            name : "urlMetacatalogApp",
            value : undefined
        }
    }];
};

Ext.reg('sitools.user.modules.mainMetacatalogModule', sitools.user.modules.mainMetacatalogModule);
