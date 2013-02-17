(function($, undefined) {

	$
			.widget(
					"ui.block",
					{
						version : "0.1.0",
						options : {
							type : "Javascript",
							container : "body",
							role : "block",

							hasMenu : false,
							editor : null,
							isOutput : true,
							outputBlockIds : [],

							isInitialized : false,
							// callbacks
							initialized : function(event, data) {
							},
							optionsChanged : function(event, options) {
								if (!$.browser.msie)
									window.console
											.debug("Event: optionsChanged from block: "
													+ options.id);
							}
						},
						_create : function() {
							this.options.isInitialized = false;
							this.options = $.recursiveFunctionTest(this.options);
							this.element.addClass("ui-block ui-widget ui-widget-content ui-corner-none"); //
							if (this.options.id) {
								this.element.attr("id", this.options.id);
							} else {
								this.element.uniqueId();
								this.options.id = this.element.attr("id");
							}
							this._initMenu(this.options.menu);
							this._setHasMenu(this.options.hasMenu);
							this._setIsOutput(this.options.isOutput);

							// this.element.append("<div class=\"ui-sort-handle
							// ui-icon ui-icon-arrow-4 ui-widget-content
							// ui-corner-all\"></div>");
							if (this.options.editor != null) {
								this._setEditor(this.options.editor);
							} else {
								this._triggerInitialized();
								this._trigger("optionsChanged", 0,
										[ this.options ]);
							}
							this.element.focusin($.proxy(function() {
								if (this.options.hasMenu) {
									this.element.find(".ui-visible-on-focus")
											.show();
								}
							}, this));
							this.element.focusout($.proxy(function() {
								this.element.find(".ui-visible-on-focus")
										.hide();
								this.syncToServer();
							}, this));

							// TODO give this Block the Focus;
							//this.lastOptions=this.options.clone(true,true);
							this.element.focusout();
							this.element.on("keypress",$.proxy(function(event){
								if(event.ctrlKey && event.keyCode==10)
									this._trigger("evaluate",1,[this.options.id]);},this)
							);

						},
						lastOptions:null,
						syncToServer : function() {
							//jTODO test if this.options == this.lastOptions
							if($("#loadingBar").css("display")=="hide")
								return;
							if(typeof wsid=="undefined")
								return
							var msg = jQuery.extend(true, {}, this.options);
							delete msg.menu;

							// FIXME syncToServer is called unnecessary some
							// times (eg. when clicking the menu)
							var content = this._addParameter("", "block", $
									.toJSON(msg));
							content = this._addParameter(content,
									"worksheetSessionId", wsid);
							$.ajax(
											"setBlock",
											{
												type : "POST",
												contentType : "application/x-www-form-urlencoded;charset=UTF-8",
												data : content
											});
							// TODO add Handling
						},
						
						createULFromNodeArrayRecursive : function(nodes) {
							var menu = $("<ul></ul>");
							for ( var x = 0; x < nodes.length; x++) {
								var menuItem = this.nodeToUL(nodes[x]);
								if (nodes[x].children.length > 0) {
									menuItem
											.append(this
													.createULFromNodeArrayRecursive(nodes[x].children));
								}
								menu.append(menuItem);
							}
							return menu;
						},
						nodeToUL : function(node) {
							var nodeItem = $("<li></li>");
							var item = $("<a></a>");

							if (node.itemClass != "") {
								nodeItem.addClass(node.itemClass);
							}

							if (node.iconClass != "") {
								var icon = $("<span></span>").addClass(
										"ui-icon " + node.iconClass);
								item.append(icon);
							}
							if (node.title) {
								if (node.text != "") {
									nodeItem.append(node.text);
								}
							} else {
								if (node.text != "") {
									item.append(node.text);
								}
								item.click(node.click);
								nodeItem.append(item);
							}
							return nodeItem;
						},
						toggleBlockContent : function() {
							var content = this.element
									.find(".ui-block-content");
							var icon = this.element
									.find(".ui-block-menu .ui-menubutton-toggle .ui-icon");
							content.toggle("blind").toggleClass(
									"ui-content-blind");
							if (content.hasClass("ui-content-blind")) {
								icon.switchClass("ui-icon-triangle-1-s",
										"ui-icon-triangle-1-w", 400,
										"easeInOutQuad");
							} else {
								icon.switchClass("ui-icon-triangle-1-w",
										"ui-icon-triangle-1-s", 400,
										"easeInOutQuad");
							}
						},
						insertEditor : function(editorOptions, index) {

							// this.options.editor=newEditor.editor("updateOptions");
						},
						/*eval : function() {
							var msg = [ this.options ];
							msg[0].editor = this.element.find(".ui-editor")
									.editor("option");
							var content = this.addParameter("", "blocks", $
									.toJSON(msg));
							content = this.addParameter(content,
									"worksheetSessionId", wsid);
							$.ajax("blockEval", {
								type : "POST",
								data : content
							}).done(
									$.proxy(function(data, status, xhr) {
										var text = xhr.responseText;
										// var
										// text=text.replace("\n","\\n").replace("\r","\\r");
										data = jQuery
												.parseJSON(xhr.responseText);
										data = $.recursiveFunctionTest(data);
										// var caller = $("#" + data.id);
										// this.removeOutputBlocks();
										this._setOptions(data[0]);
										var worksheet = $("#"
												+ this.options.worksheetId);
										for ( var x = 1; x < data.length; x++) {
											var block = worksheet.worksheet(
													"appendBlock", data[x]);
										}
									}, this));
						},
						addParameter : function(res, key, value) {
							if (res != "")
								res += "&";
							res += encodeURIComponent(key) + "="
									+ encodeURIComponent(value);
							return res;
						},*/
						getOutputBlockIds : function() {
							return this.options.outputBlockIds;
						},
						removeOutputBlocks : function() {
							var worksheet = $("#" + this.options.worksheetId);
							for ( var x = 0; x < this.options.outputBlockIds.length; x++) {
								worksheet.worksheet("removeBlockById",
										this.options.outputBlockIds[x]);
							}
							this.options.outputBlockIds = [];
							this
									._trigger("optionsChanged", 0,
											[ this.options ]);
						},
						updateOptions : function() {
							this.options.editor = $(
									"#" + this.options.editor.id).editor(
									"updateOptions");
						},
						_setOption : function(key, value) {
							switch (key) {
							case "id":
								break;
							case "worksheetId":
								break;
							case "hasMenu":
								this._setHasMenu(value);
								break;
							case "menu":
								// TODO solve bug with missing click handler
								// after reinit
								// this._initMenu(value);
								break;
							case "editor":
								this._setEditor(value);
								break;
							case "isOutput":
								break;
							case "outputBlockIds":
								this.removeOutputBlocks();
								break;
							case "mark":
								break;

							default:
								break;
							}
							this._super(key, value);
							this._trigger("optionsChanged", 0, this.options);
						},
						_initMenu : function(menuOptions) {
							var menu = this.element.find(".ui-block-menu ");
							if (menu.length > 0) {
								menu.menubar("destroy");
								menu.remove();
							}
							if (menuOptions != null && menuOptions.length > 0) {
								var blockMenu = this
									.createULFromNodeArrayRecursive(menuOptions);
								blockMenu.addClass(
								"ui-block-menu ui-visible-on-focus")
									.menubar();
								this.element.prepend(blockMenu);
							} 
							
						},
						_setHasMenu : function(hasMenu) {
							var menu = this.element.find(".ui-block-menu");
							if (hasMenu == false) {
								menu.hide();
							} else {
								menu.show();
							}
						},
						_setEditor : function(editorOptions) {
							var editor = this.element.find(".ui-editor");
							if (editor.length > 0) {
								editor.editor("destroy");
								editor.remove();
								this.element.find(".ui-block-content").remove();
							}
							if (editorOptions != null) {
								var blockContentContainer = $("<div></div>").addClass("ui-block-content");
								this.element.append(blockContentContainer);

								var newEditor = $("<div></div>");
								blockContentContainer.append(
										newEditor);
								newEditor.one("editorinitialized", $.proxy(
										function() {
											this._triggerInitialized();
											this._trigger("optionsChanged", 0,
													[ this.options ]);
										}, this));
								newEditor.editor(editorOptions);
								newEditor.bind("editoroptionschanged", $.proxy(
										function(event, options) {
											this._editorOptionsChanged(options)
										}, this));
								newEditor.bind("editorcontentchanged",function(){
										$(".ui-worksheet").worksheet("setDirty",true)});
								newEditor.on
							}

						},
						_javaSetDirty : function(content, dirty) {
							if (typeof setDirty == 'function') {
								if (dirty)
									setDirty(true);
							}
						},
						_editorOptionsChanged : function(options) {
							this.options.editor = options;
							this
									._trigger("optionsChanged", 0,
											[ this.options ]);

						},
						_destroy : function() {
							this._setEditor(null);
							this.element.empty();
							this.element
									.removeClass("ui-block ui-widget ui-widget-content ui-corner-all");
							this.element.removeAttr("id", "");
							this.element.removeAttr("tabindex", "");
						},
						_addParameter : function(res, key, value) {
							if (res != "")
								res += "&";
							res += encodeURIComponent(key) + "="
									+ encodeURIComponent(value);
							return res;
						},
						_triggerInitialized : function() {
							this._setOption("isInitialized", true);
							if (!$.browser.msie)
								window.console
										.debug("Event: initialized from Block");
							this
									._trigger("initialized", 0,
											[ this.options.id ]);
						},

						_setIsOutput:function(isOutput){
							if(isOutput){
								this.element.addClass("ui-output");
							}else{
								this.element.removeClass("ui-output");	
							}
						},
						switchBlock:function(name){
							this.element.closest(".ui-worksheet").worksheet("switchBlock",{type:name,blockId:this.options.id})
						}

					});

}(jQuery));