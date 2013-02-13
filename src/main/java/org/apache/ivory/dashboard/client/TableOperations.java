package org.apache.ivory.dashboard.client;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class TableOperations {
	static FlexTable visibleForm = new FlexTable();

	public static void insertText(int row, int column, String text) {
		Label lb = new Label();
		lb.setText(text);
		visibleForm.setWidget(row, column, lb);

	}

	public static void insertWidget(int row, int column, Widget w) {
		visibleForm.setWidget(row, column, w);
	}

	public static String getText(int row, int column) {
		Label lb = (Label) visibleForm.getWidget(row, column);
		if (lb != null)
			return lb.getText();
		return "";
	}

	public static void applyStyle() {

		visibleForm.addStyleName("visibleForm");
		int max = 0;
		int i = 0;
		for (i = 0; i < visibleForm.getRowCount(); i++) {
			if (visibleForm.getCellCount(i) > max) {
				max = visibleForm.getCellCount(i);
			}
		}
		for (i = 0; i < visibleForm.getRowCount(); i++) {
			for (int j = visibleForm.getCellCount(i); j < max; j++) {
				visibleForm.insertCell(i, j);
			}
			if (i % 2 == 1) {
				visibleForm.getRowFormatter().removeStyleName(i,
						"visibleForm-EvenRow");
				visibleForm.getRowFormatter().addStyleName(i,
						"visibleForm-OddRow");

			} else {
				visibleForm.getRowFormatter().removeStyleName(i,
						"visibleForm-OddRow");
				visibleForm.getRowFormatter().addStyleName(i,
						"visibleForm-EvenRow");
			}
		}

	}

	public static void duplicateRow(int row, boolean flag) {
		// Finding how many rows to copy
		int rowsToBeCopied = 1;
		int n = 0;
		// Traversing in the row to the valid cell in current row
		for (n = 0; n < visibleForm.getCellCount(row); n++) {
			if (visibleForm.isCellPresent(row, n))
				if (!visibleForm.getText(row, n).equals(""))
					break;

		}
		int k = row + 1;
		// calculating how many rows together make this element
		while (true) {
			if (k >= visibleForm.getRowCount() - 1)
				break;
			if (visibleForm.isCellPresent(k, n)) {
				if (!visibleForm.getText(k, n).equals(""))
					break;
			}
			if (visibleForm.isCellPresent(k, n - 1)) {

				if (!visibleForm.getText(k, n - 1).equals(""))
					break;
			}
			rowsToBeCopied++;
			k++;
		}

		// Duplicate those many rows

		for (int counter = 0; counter < rowsToBeCopied; counter++) {
			visibleForm.insertRow(row + rowsToBeCopied);

		}
		for (int counter = 0; counter < rowsToBeCopied; counter++) {

			for (int i = 0; i < visibleForm.getCellCount(row + counter); i++) {
				Widget above = visibleForm.getWidget(row + counter, i);
				if (above != null) {
					if (above instanceof ListBox) {
						ListBox w = new ListBox();
						for (int i1 = 0; i1 < ((ListBox) above).getItemCount(); i1++) {
							w.addItem(((ListBox) above).getItemText(i1));

						}
						visibleForm.setWidget(row + counter + rowsToBeCopied,
								i, w);
					} else if (above instanceof Button) {
						if (!flag) {
							String text = ((Button) above).getText();
							text = text.replace("Add", "Remove");
							Button removeButton = new Button(text);
							/*
							 * Add click event listeners
							 */
							class Handler implements ClickHandler {

								public void onClick(ClickEvent event) {
									int row = TableOperations.visibleForm
											.getCellForEvent(event)
											.getRowIndex();
									int col = TableOperations.visibleForm
											.getCellForEvent(event)
											.getCellIndex();
									int validcell;
									for ( validcell = 0; validcell < col; validcell++) {

										if (visibleForm.getWidget(row,
												validcell) != null ) {
											if(visibleForm.getWidget(row,
													validcell) instanceof Label)
											{
												break;
											}
										}

									}
									
									//removing rows 
									visibleForm.removeRow(row);
									while(visibleForm.getWidget(row,validcell) == null)
									{
										visibleForm.removeRow(row);
									}
									TableOperations.applyStyle();
								}
							}

							Handler handler = new Handler();
							removeButton.addClickHandler(handler);
							visibleForm.setWidget(row + counter, i,
									removeButton);
						}
						visibleForm.setWidget(row + counter + rowsToBeCopied,
								i, above);
						;
					} else if (above instanceof TextBox) {

						TextBox w = new TextBox();
						w = addListener(w, row + counter, i);
						visibleForm.setWidget(row + counter + rowsToBeCopied,
								i, w);
					} else if (above instanceof Label) {
						Label lb = new Label();
						lb.setText(((Label) above).getText());
						visibleForm.setWidget(row + counter + rowsToBeCopied,
								i, lb);
					}
				} else {
					visibleForm.setText(row + counter + rowsToBeCopied, i,
							visibleForm.getText(row + counter, i));
				}
			}

		}

	}

	private static TextBox addListener(TextBox w, int i, int j) {

		String element = TableOperations.visibleForm.getText(i, j - 1);

		String parent = "";
		j = j - 2;

		if (!TableOperations.visibleForm.getText(i, j).equals("")) {
			;
		} else if (TableOperations.visibleForm.getWidget(i, j) != null) {
			while (true) {
				if (!TableOperations.visibleForm.getText(i, j).equals("")) {
					break;
				}
				j = j - 2;
			}
		} else {
			while (i > 0) {
				if (!TableOperations.visibleForm.getText(i, j).equals("")) {
					break;
				}
				i--;
			}
		}
		parent += TableOperations.visibleForm.getText(i, j);

		/*
		 * Insert "parent+element" string as key and pattern as value into the
		 * hash map
		 */

		String pattern = ParseXSD.namePatternMap.get(parent + element);
		if (pattern != null) {
			class TextBoxBlurHandler implements BlurHandler {

				public void onBlur(BlurEvent event) {

					TextBox tb = ((TextBox) event.getSource());
					String userInput = tb.getText();
					// If userinput is null , do nothing and return
					if (userInput.equals(""))
						return;
					boolean flag = false;
					int i, j = 0;
					for (i = 0; i < TableOperations.visibleForm.getRowCount(); i++) {
						for (j = 0; j < TableOperations.visibleForm
								.getCellCount(i); j++) {
							if (TableOperations.visibleForm.getWidget(i, j) == tb) {
								flag = true;
								break;
							}
						}
						if (flag) {
							break;
						}
					}

					String element = TableOperations.visibleForm.getText(i,
							j - 1);

					String parent = "";
					j = j - 2;

					if (!TableOperations.visibleForm.getText(i, j).equals("")) {
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

					String pattern = ParseXSD.namePatternMap.get(parent
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
			w.addBlurHandler(blurHandler);
			return w;
		}

		return w;

	}

	public static void insertAddButton(final int row, int i, String name) {
		Button add = new Button("Add " + name);
		visibleForm.setWidget(row, i, add);

		class MyHandler implements ClickHandler {
			/**
			 * Fired when the user clicks on the clicks on the create Button.
			 */
			public void onClick(ClickEvent event) {
				int row = visibleForm.getCellForEvent(event).getRowIndex();
				duplicateRow(row, false);
				TableOperations.applyStyle();

			}
		}
		MyHandler handler = new MyHandler();
		add.addClickHandler(handler);

	}

}
