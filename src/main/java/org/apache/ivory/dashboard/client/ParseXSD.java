package org.apache.ivory.dashboard.client;

import java.util.HashMap;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class ParseXSD {

	// Create a hash map
	static HashMap<String, Node> nameTypeMap = new HashMap<String, Node>();
	static HashMap<String, String> namePatternMap = new HashMap<String, String>();
	static Node xmlns;

	public static void parse(String xsd) {
		// build DOM
		Document dom = buildDOM(xsd);
		// Get the schema
		Node schema = dom.getDocumentElement();
		xmlns = schema.getAttributes().getNamedItem("targetNamespace");
		Node start = null;
		/*
		 * finding the "element" tag among the children of schema for starting
		 * point for parsing. Mean while store the name - type mapping in a hash
		 * map
		 */

		for (Node i = schema.getFirstChild(); i != null; i = i.getNextSibling()) {
			if (i.getNodeType() != Node.ELEMENT_NODE)
				continue;
			/*
			 * Get the start node . Exclude it from inserting into the hash map
			 */
			if (i.getNodeName().equals("xs:element")) {
				start = i;
				continue;
			}

			nameTypeMap.put(((Element) i).getAttribute("name"), i);
		}

		// parse recursively
		// Load table into the container

		recursiveParse(start, 0, 0, true);
		RootPanel.get("mainContainer").add(TableOperations.visibleForm);

		// generate entity according to user input
		Button generateEntityButton = new Button("Create Entity");
		final Button submitEntityButton = new Button("Submit");
		class MyHandler implements ClickHandler, KeyUpHandler {

			public void onClick(ClickEvent event) {
				if (InputValidator.validate()) {
					EntityGenerator.generateXml();
					RootPanel.get("mainContainer").add(submitEntityButton);

				}

			}

			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					//
				}
			}
		}
		MyHandler handler = new MyHandler();
		generateEntityButton.addClickHandler(handler);
		generateEntityButton.addKeyUpHandler(handler);

		RootPanel.get("mainContainer").add(generateEntityButton);

		TableOperations.applyStyle();

	}

	static int gRow = 0;
	static int gCol = 0;

	private static void recursiveParse(Node node, int row, int col,
			boolean CreateTagButtonFlag) {

		String name = ((Element) node).getAttribute("name");
		String type = ((Element) node).getAttribute("type");

		String maxOccurs = ((Element) node).getAttribute("maxOccurs");
		String minOccurs = ((Element) node).getAttribute("minOccurs");

		/*
		 * If minoccurs is zero, create an add button instead of parsing it and
		 * return
		 */
		
		//Static hack for rendering of feed entity in a better way
		if(name.equals("groups") || name.equals("availabilityFlag") || name.equals("timezone"))
		{
			CreateTagButtonFlag = false;
		}
		
		//Static hack for rendering of process entity in a better way
		if(name.equals("timeout"))
		{
			CreateTagButtonFlag = false;
		}

		
		if (minOccurs != null) {
			if (minOccurs.equals("0") && CreateTagButtonFlag) {
				addCreateTagButton(row, col, node);
				return;
			}
			
		}
		else if(minOccurs == null)
		{
			name = name+="*";
		}
		// get the node name and insert it into table

		TableOperations.insertText(row, col, name);

		/*
		 * handling cases where type will be mentioned internally
		 */

		if (type == null) {
			
			for (Node i = node.getFirstChild(); i != null; i = i
					.getNextSibling()) {
				if (i.getNodeType() != Node.ELEMENT_NODE)
					continue;

				if (i.getNodeName().equals("xs:simpleType")) {
					processSimpleType(i, row, col);
					return;
				}

			}
		}
		// Handling string type elements

		if (type != null && type.equals("xs:string")) {
			TableOperations.insertWidget(row, col + 1, new TextBox());
			return;
		}

		// Extract node from hash table
		node = nameTypeMap.get(type);

		if (node == null)
			return;
		// Processing child which is of simple type and return
		if (node.getNodeName().equals("xs:simpleType")) {
			processSimpleType(node, row, col);
			return;
		}

		// parse child nodes recursively for complex type

		for (Node i = node.getFirstChild(); i != null; i = i.getNextSibling()) {

			if (i.getNodeType() != Node.ELEMENT_NODE)
				continue;

			// process child nodes which are attributes
			// attribute nodes will be simple nodes

			if (i.getNodeName().equals("xs:attribute")) {
				processAttribute(i, row, col + 1);
				col += 2;
			}

			// process child nodes which are sequences
			if (i.getNodeName().equals("xs:sequence")) {
				for (Node j = i.getFirstChild(); j != null; j = j
						.getNextSibling()) {
					if (j.getNodeType() != Node.ELEMENT_NODE)
						continue;

					gRow++;
					TableOperations.visibleForm.insertRow(gRow);
					recursiveParse(j, gRow, col + 1, true);
				}

			}
	
			

		}
		/*
		 * Processing max occurs
		 */
		if (maxOccurs != null) {
			if (maxOccurs.equals("unbounded")) {
				TableOperations.insertAddButton(row, col + 1, name);
			}
			// add code here if maxoccurs is a fixed integer. right now schema definition dont have it
		}
		/*
		 * Processing min occurs
		 */
		if (minOccurs != null) {
			for (int i = 0; i < Integer.parseInt(minOccurs) - 1; i++) {
				TableOperations.duplicateRow(gRow, true);
				gRow++;
			}

		}

	}

	private static void addCreateTagButton(int row, int col, Node node) {
		TableOperations.visibleForm.setWidget(row, col,
				createAddButtonForTag(node));

	}

	private static Widget createAddButtonForTag(final Node node) {

		String name = ((Element) node).getAttribute("name");

		/*
		 * creating button with "Add tag" label in it
		 */
		Button button = new Button("Add " + name);
		/*
		 * Add click event listeners
		 */
		class Handler implements ClickHandler {
			/**
			 * Fired when the user clicks on the clicks on the create tag
			 * Button.
			 */
			public void onClick(ClickEvent event) {
				int row = TableOperations.visibleForm.getCellForEvent(event)
						.getRowIndex();
				int col = TableOperations.visibleForm.getCellForEvent(event)
						.getCellIndex();
				/*
				 * function createElement creates the element in the table by
				 * replacing the add button
				 */
				gRow = row;
				gCol = col;
				recursiveParse(node, row, col, false);
				TableOperations.applyStyle();
			}
		}
		Handler handler = new Handler();
		button.addClickHandler(handler);

		return button;
	}

	private static void processSimpleType(Node i, int row, int col) {

		TableOperations.insertWidget(row, col + 1, makeWidget(i, row, col + 1));

	}

	private static void processAttribute(Node i, int row, int col) {

		// Processing attributes that belong to simple type
		String type = ((Element) i).getAttribute("type");
		String name = ((Element) i).getAttribute("name");
		String use = ((Element) i).getAttribute("use");
		String defaultvalue = ((Element) i).getAttribute("default");

		if (use != null)
			if (use.equals("required")) {
				name += "*";
			}
		/*
		 * Processing different attribute types
		 */

		if (type != null) {
			// string type
			if (type.equals("xs:string")) {
				TableOperations.insertText(row, col, name);
				TableOperations.insertWidget(row, col + 1, new TextBox());
				return;
			}
			// simple type
			// checking in hash table for simple type
			Node simpleAttribute = nameTypeMap.get(type);
			// If key present in hash table as a simple type
			if (simpleAttribute != null) {
				// insert name into table
				TableOperations.insertText(row, col, name);
				TableOperations.insertWidget(row, col + 1,
						makeWidget(simpleAttribute, row, col + 1));
				return;
			}
		} else if (type == null) {
			for (Node j = i.getFirstChild(); j != null; j = j.getNextSibling()) {
				if (j.getNodeType() != Node.ELEMENT_NODE)
					continue;

				if (j.getNodeName().equals("xs:simpleType")) {
					TableOperations.insertText(row, col, name);
					TableOperations.insertWidget(row, col + 1,
							makeWidget(j, row, col + 1));
					return;
				}

			}
		}

	}

	private static Widget makeWidget(Node simpleAttribute, int row, int col) {

		int k = 0;
		boolean flag = true;
		Widget w = new Widget();

		NodeList tempList = simpleAttribute.getChildNodes();
		while (k < tempList.getLength()) {
			Node currentNode = tempList.item(k);
			if (currentNode.getNodeType() != Node.ELEMENT_NODE) {
				k++;
				continue;
			}

			if (currentNode.getNodeName().equals("xs:restriction")) {

				ListBox lb = new ListBox();
				lb.addItem("");
				lb.setSelectedIndex(0);
				NodeList restrictionNodeList = currentNode.getChildNodes();
				// For restrictions which mentions only base and has no patters
				// or enumerations
				if (restrictionNodeList.getLength() == 0) {

					if (((Element) currentNode).getAttribute("base").equals(
							"xs:string")) {
						TextBox tb = new TextBox();
						return tb;
					}

				}
				// making widget for integers
				if (((Element) currentNode).getAttribute("base").equals(
						"xs:unsignedShort")) {
					// find min and max if they are present
					int min = 0;
					int max = 0;
					for (Node m = currentNode.getFirstChild(); m != null; m = m
							.getNextSibling()) {

						if (m.getNodeName().equals("xs:minInclusive")) {
							min = Integer.parseInt(((Element) m)
									.getAttribute("value"));
						}
						if (m.getNodeName().equals("xs:maxInclusive")) {
							max = Integer.parseInt(((Element) m)
									.getAttribute("value"));
						}
					}
					if (min < max) {
						for (int m = min; m <= max; m++)
							lb.addItem(m + "");
						return lb;
					} else {
						TextBox tb = new TextBox();
						return tb;
					}
				}

				for (int len = 0; len < restrictionNodeList.getLength(); len++) {
					if (restrictionNodeList.item(len).getNodeType() != Node.ELEMENT_NODE)
						continue;
					/*
					 * getting regular expressions and storing it for
					 * validations
					 */
					if (restrictionNodeList.item(len).getNodeName()
							.equals("xs:pattern")) {

						final String pattern = ((Element) restrictionNodeList
								.item(len)).getAttribute("value");

						/*
						 * Find element for which pattern is applicable and find
						 * elements parent Insert into hash table for future
						 */
						String element = TableOperations.visibleForm.getText(
								row, col - 1);

						String parent = "";
						int i = row, j = col - 2;

						if (!TableOperations.visibleForm.getText(i, j).equals(
								"")) {
							;
						} else if (TableOperations.visibleForm.getWidget(i, j) != null) {
							while (true) {
								if (!TableOperations.visibleForm.getText(i, j)
										.equals("")) {
									break;
								}
								j = j - 2;
							}
						} else {
							while (i > 0) {
								if (!TableOperations.visibleForm.getText(i, j)
										.equals("")) {
									break;
								}
								i--;
							}
						}
						parent += TableOperations.visibleForm.getText(i, j);

						/*
						 * Insert "parent+element" string as key and pattern as
						 * value into the hash map
						 */
						namePatternMap.put(parent + element, pattern);
						class TextBoxBlurHandler implements BlurHandler {

							public void onBlur(BlurEvent event) {

								TextBox tb = ((TextBox) event.getSource());
								String userInput = tb.getText();
								// If userinput is null , do nothing and return
								if (userInput.equals(""))
									return;
								boolean flag = false;
								int i, j = 0;
								for (i = 0; i < TableOperations.visibleForm
										.getRowCount(); i++) {
									for (j = 0; j < TableOperations.visibleForm
											.getCellCount(i); j++) {
										if (TableOperations.visibleForm
												.getWidget(i, j) == tb) {
											flag = true;
											break;
										}
									}
									if (flag) {
										break;
									}
								}

								String element = TableOperations.visibleForm
										.getText(i, j - 1);

								String parent = "";
								j = j - 2;

								if (!TableOperations.visibleForm.getText(i, j)
										.equals("")) {
									;
								} else if (TableOperations.visibleForm
										.getWidget(i, j) != null) {
									while (true) {
										if (!TableOperations.visibleForm
												.getText(i, j).equals("")) {
											break;
										}
										j = j - 2;
									}
								} else {
									while (i > 0) {
										if (!TableOperations.visibleForm
												.getText(i, j).equals("")) {
											break;
										}
										i--;
									}
								}
								parent += TableOperations.visibleForm.getText(
										i, j);

								/*
								 * Insert "parent+element" string as key and
								 * pattern as value into the hash map
								 */

								String pattern = namePatternMap.get(parent
										+ element);

								if (!userInput.matches(pattern)) {
									// alert box for invalid input
									Window.alert("Invalid Input. Please Enter Again");
									tb.selectAll();
									tb.setFocus(true);
								}

							}
						}

						TextBoxBlurHandler blurHandler = new TextBoxBlurHandler();
						TextBox tb = new TextBox();
						tb.addBlurHandler(blurHandler);

						return tb;
					}

					/*
					 * getting enumerations for making list boxes
					 */
					else if (restrictionNodeList.item(len).getNodeName()
							.equals("xs:enumeration")) {
						if (flag) {

							String item = ((Element) restrictionNodeList
									.item(len)).getAttribute("value");
							lb.addItem(item);

							flag = false;
						} else {
							String item = ((Element) restrictionNodeList
									.item(len)).getAttribute("value");
							lb.addItem(item);
						}
					}

				}
				return lb;
			}
			k++;
		}

		return w;
	}

	private static Document buildDOM(String xsd) {

		// parse the xsd document into a DOM
		Document dom = XMLParser.parse(xsd);
		return dom;
	}

}
