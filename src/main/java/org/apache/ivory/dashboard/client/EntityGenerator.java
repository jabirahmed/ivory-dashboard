package org.apache.ivory.dashboard.client;

import java.io.IOException;
import java.io.StringWriter;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.XMLParser;

public class EntityGenerator {
	static Label labelForXml = new Label();
	public static void generateXml() {
		/*
		 * parsing document using the user filled flextable
		 */

		Document doc = XMLParser.createDocument();
		String rootName = (TableOperations.visibleForm.getText(0, 0));
		if (rootName.endsWith("*"))
			rootName = rootName.replace("*", "");
		Element root = doc.createElement(rootName); // getting
		// root

		root.setAttribute("xmlns", ParseXSD.xmlns.toString());
		doc.appendChild(root);

		recursiveXML(doc, root, 0, 0);
		handleNode(doc);
		viewXML(doc);

	}

	/*
	 * This function is used to create the xml from the filled up form
	 */
	private static void recursiveXML(Document doc, Element root, int i, int j) {

		if (!TableOperations.visibleForm.isCellPresent(i, j))
			return;

		// adding attributes
		// by checking the third cell from current cell, checking for attributes
		if (TableOperations.visibleForm.isCellPresent(i, j)
				&& TableOperations.visibleForm.isCellPresent(i, j + 1)
				&& TableOperations.visibleForm.isCellPresent(i, j + 2)) {
			// if current cell is not a widget and third cell is a widget
			if ((TableOperations.visibleForm.getWidget(i, j) instanceof Label)
					&& (TableOperations.visibleForm.getWidget(i, j + 2) != null)) {

				// set all attributes by traversing the current row
				for (int k = j + 1; k < TableOperations.visibleForm
						.getCellCount(i); k += 2) {

					if (!TableOperations.visibleForm.isCellPresent(i, k + 1))
						continue;
					if (!TableOperations.visibleForm.isCellPresent(i, k))
						continue;

					Widget cellWidget = TableOperations.visibleForm.getWidget(
							i, k + 1);

					if (cellWidget instanceof ListBox) {
						String name = TableOperations.getText(i, k);
						String value = ((ListBox) TableOperations.visibleForm
								.getWidget(i, k + 1))
								.getValue(((ListBox) TableOperations.visibleForm
										.getWidget(i, k + 1))
										.getSelectedIndex());
						if (name.endsWith("*"))
							name = name.replace("*", "");
						if (!value.equals(""))
							root.setAttribute(name, value);

					} else if (cellWidget instanceof TextBox) {

						String name = TableOperations.getText(i, k);
						String value = ((TextBox) TableOperations.visibleForm
								.getWidget(i, k + 1)).getText();

						if (name.endsWith("*"))
							name = name.replace("*", "");
						if (!value.equals(""))
							root.setAttribute(name, value);

					}

				}

			}

		}
		// Adding nodes which has no attributes and children
		if (TableOperations.visibleForm.isCellPresent(i, j)
				&& TableOperations.visibleForm.isCellPresent(i, j + 1)) {
			// if current cell is not a widget and second cell is a widget
			if ((TableOperations.visibleForm.getWidget(i, j) instanceof Label)
					&& (TableOperations.visibleForm.getWidget(i, j + 1) != null)) {
				Widget cellWidget = TableOperations.visibleForm.getWidget(i,
						j + 1);

				if (cellWidget instanceof ListBox) {

					String value = ((ListBox) TableOperations.visibleForm
							.getWidget(i, j + 1))
							.getValue(((ListBox) TableOperations.visibleForm
									.getWidget(i, j + 1)).getSelectedIndex());
					if (value.equals("")) {
						root.getParentNode().removeChild(root);
					} else
						root.appendChild(doc.createTextNode(value));

				} else if (cellWidget instanceof TextBox) {

					String value = ((TextBox) TableOperations.visibleForm
							.getWidget(i, j + 1)).getText();
					if (value.equals("")) {
						root.getParentNode().removeChild(root);
					} else
						root.appendChild(doc.createTextNode(value));
				}

			}
		}
		// adding children
		// by checking the next column, checking for child/children
		for (int k = i + 1; k < TableOperations.visibleForm.getRowCount(); k++) {
			if (TableOperations.visibleForm.isCellPresent(k, j)) {
				if ((TableOperations.visibleForm.getWidget(k, j) instanceof Label)
						|| (TableOperations.visibleForm.getWidget(k, j) instanceof Button))
					return;
			}
			if (TableOperations.visibleForm.isCellPresent(k, j + 1)) {

				if (TableOperations.visibleForm.getWidget(k, j + 1) instanceof Label) {
					String name = TableOperations.getText(k, j + 1);
					if (name.endsWith("*"))
						name = name.replace("*", "");
					Element child = doc.createElement(name); // new child
					root.appendChild(child); // appending to current root
					recursiveXML(doc, child, k, j + 1);
				}
			}
		}
	}

	/*
	 * function for viewing the generated xml
	 */
	private static void viewXML(Document doc) {

		
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"\n"+ doc.toString();

		
		labelForXml.getElement().getStyle().setProperty("whiteSpace", "pre");
		labelForXml.setText(xml);
		RootPanel.get("mainContainer").add(labelForXml);
		
		
	}

	

	private static void handleNode(Node node) {
		if (!node.hasChildNodes() && !node.hasAttributes()) {

			node.getParentNode().removeChild(node);
			return;
		}
		// recurse the children
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			if (node.getChildNodes().item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			handleNode(node.getChildNodes().item(i));
		}
	}

}
