///////////////////////////////////////////////////////////////////////////
// Copyright © 2014 Esri. All Rights Reserved.
//
// Licensed under the Apache License Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
///////////////////////////////////////////////////////////////////////////

define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dojo/_base/array',
  'dojo/_base/html',
  'dojo/_base/query',
  'dojo/on',
  'dijit/_WidgetsInTemplateMixin',
  'jimu/BaseWidgetSetting',
  'jimu/dijit/TabContainer',
  'jimu/dijit/SimpleTable',
  'jimu/dijit/LayerFieldChooser',
  'jimu/dijit/IncludeButton',
  'jimu/dijit/URLInput',
  'dijit/form/NumberTextBox',
  'dijit/form/TextBox',
  'dijit/form/Select',
  'esri/request'
],
function(declare, lang, array, html, query, on, _WidgetsInTemplateMixin,BaseWidgetSetting,TabContainer,
  SimpleTable,LayerFieldChooser,IncludeButton,URLInput,NumberTextBox,TextBox,Select,esriRequest) {/*jshint unused: false*/
  return declare([BaseWidgetSetting,_WidgetsInTemplateMixin], {
    baseClass: 'jimu-widget-query-setting',
    _url:"",

    postCreate:function(){
      this.inherited(arguments);
      this.own(on(this.btnBrowse,'click',lang.hitch(this,this._refreshLayerFields)));
      this.own(on(this.includeButton,'Click',lang.hitch(this,this.onIncludeClick)));
      this._initTables();
      this.setConfig(this.config);
    },

    startup: function(){
      this.inherited(arguments);

      this.tabContainer = new TabContainer({
        tabs:[{
          title:this.nls.input,
          content:this.inputTabNode
        },{
          title:this.nls.output,
          content:this.outputTabNode
        }],
        isNested: true
      },this.content);
      this.tabContainer.startup();
    },

    _initTables:function(){
      this.own(on(this.allFieldsTable,'Select',lang.hitch(this,function(){
        this.includeButton.enable();
      })));
      this.own(on(this.allFieldsTable,'Clear',lang.hitch(this,function(){
        this.includeButton.disable();
      })));
      this.own(on(this.allFieldsTable,'DblClick',lang.hitch(this,function(){
        this.includeButton.enable();
        this.includeButton.onClick();
      })));
    },

    setConfig:function(config){
      this.config = config;
      this.resetAll();
      if(!this.config){
        return;
      }
      this.shareCbx.checked = config.shareResult === true;
      this.defaultValue.set('value',lang.trim(this.config.defaultValue)||"");
      this.zoomScale.set('value',this.config.zoomscale);

      if(this.config.layer){
        this.layerUrl.set('value', lang.trim(this.config.layer.url) || "");
        this._url = this.layerUrl.get('value');
        this.expression.set('value', lang.trim(this.config.layer.expression) || "");
        var linkfield = this.config.layer.linkfield;
        if (linkfield) {
          var selectedOption = {
            name: linkfield,
            label: linkfield
          };
          this.linkFieldSelect.addOption(selectedOption);
          this.linkFieldSelect.set('value', linkfield);
        }
        this.searchLabel.set('value',lang.trim(this.config.layer.textsearchlabel)||"");
        this.searchHint.set('value',lang.trim(this.config.layer.textsearchhint)||"");

        var displayFields = this.config.layer.fields && this.config.layer.fields.field;
        if(displayFields && displayFields.length > 0){
          this._addDisplayFields(displayFields,this.config.layer.titlefield||'');
        }
      }
      if(this._url){
        this.allFieldsTable.refresh(this._url);
      }
    },

    onIncludeClick:function(){
      var tr = this.allFieldsTable.getSelectedRow();
      if(tr){
        var fieldInfo = tr.fieldInfo;
        this._createDisplayField(fieldInfo);
      }
    },

    _refreshLayerFields:function(){
      var value = lang.trim(this.layerUrl.get('value'));
      if(value !== this._url){
        this._url = value;
        this.resetTables();
        this.allFieldsTable.refresh(this._url);
      }
    },

    resetAll:function(){
      this.resetTables();
      this._url = '';
      this.layerUrl.set('value',this._url);
      this.expression.set('value','');
      this.defaultValue.set('value','');
      this.searchLabel.set('value','');
      this.searchHint.set('value','');
      this.zoomScale.set('value',10000);
    },

    resetTables:function(){
      this._resetLinkFieldSelect();
      this.allFieldsTable.clear();
      this.displayFieldsTable.clear();
    },

    _setLinkFieldSelectOptions:function(fields,selectedFieldInfo){
      this._resetLinkFieldSelect();
      for(var i=0;i<fields.length;i++){
        var name = fields[i].name;
        var option = {value: name, label: name};
        this.linkFieldSelect.addOption(option);
      }
      if(selectedFieldInfo){
        this.linkFieldSelect.set('value',selectedFieldInfo.name);
      }
    },

    _resetLinkFieldSelect:function(){
      var options = this.linkFieldSelect.options;
      array.forEach(options,lang.hitch(this,function(item,index){
        if(index !== 0){
          this.linkFieldSelect.removeOption(item);
        }
      }));
    },

    _addDisplayFields:function(fieldInfos,titleField){
      array.forEach(fieldInfos,lang.hitch(this,function(fieldInfo){
        this._createDisplayField(fieldInfo,titleField);
      }));
    },

    _createDisplayField:function(fieldInfo,titleField){
      var rowData = {
        name:fieldInfo.name,
        alias:fieldInfo.alias||fieldInfo.name,
        title:fieldInfo.name === titleField
      };
      this.displayFieldsTable.addRow(rowData);
    },

    _getTitleField:function(){
      var result = null;
      var rowDatas = this.displayFieldsTable.getRowDataArrayByFieldValue('title',true);
      if(rowDatas.length > 0){
        var rowData = rowDatas[0];
        result = rowData.name;
      }
      return result;
    },

    validate:function(){
      if(!this._url){
        return false;
      }
      if(!lang.trim(this.expression.get('value'))){
        return false;
      }
      var rowsData = this.displayFieldsTable.getData();
      var fieldsArray = array.map(rowsData,lang.hitch(this,function(item){
        return {name:item.name,alias:item.alias};
      }));
      if(fieldsArray.length === 0){
        return false;
      }
      return true;
    },

    getConfig: function () {
      var valid = this.validate();
      if(!valid){
        return false;
      }
      this.config = this.config ? this.config : {};
      this.config.defaultValue = lang.trim(this.defaultValue.get('value'));
      this.config.zoomscale = this.zoomScale.get('value');
      this.config.shareResult = this.shareCbx.checked;

      if(!this.config.layer){
        this.config.layer = {
          url:'',
          expression:'',
          textsearchlabel:'',
          textsearchhint:'',
          titlefield:'',
          linkfield:'',
          fields:{
            all:false,
            field:[]
          }
        };
      }
      
      this.config.layer.url = lang.trim(this.layerUrl.get('value'));
      this.config.layer.expression = lang.trim(this.expression.get('value'));
      var linkfield = this.linkFieldSelect.get('value');
      if(linkfield === 'nodata'){
        linkfield = "";
      }
      this.config.layer.linkfield = linkfield;
      var rowsData = this.displayFieldsTable.getData();
      var fieldsArray = array.map(rowsData,lang.hitch(this,function(item){
        return {name:item.name,alias:item.alias};
      }));
      if(!this.config.layer.fields){
        this.config.layer.fields = {
          field:[]
        };
      }
      this.config.layer.fields.field = fieldsArray;
      this.config.layer.titlefield = this._getTitleField();
      
      this.config.layer.textsearchlabel = lang.trim(this.searchLabel.get('value'));
      this.config.layer.textsearchhint = lang.trim(this.searchHint.get('value'));

      return this.config;
    }
    
  });
});