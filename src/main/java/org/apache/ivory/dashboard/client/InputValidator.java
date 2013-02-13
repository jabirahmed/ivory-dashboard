package org.apache.ivory.dashboard.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class InputValidator {

	public static boolean validate() {

		boolean flag = false;
		// Traverse the whole table and find whether all the essential fields
		// are filled up
		for (int i = 0; i < TableOperations.visibleForm.getRowCount(); i++) {
			for (int j = 0; j < TableOperations.visibleForm.getCellCount(i); j++) {
				if (TableOperations.visibleForm.isCellPresent(i, j)
						&& TableOperations.visibleForm.isCellPresent(i, j + 1)) {
					if (TableOperations.visibleForm.getText(i, j) != null) {
						if (TableOperations.visibleForm.getWidget(i, j + 1) != null) {
							Widget w = TableOperations.visibleForm.getWidget(i,
									j + 1);
							if (w instanceof TextBox) {
								if (((TextBox) w).getText().equals("")) {
									Label lb = (Label) TableOperations.visibleForm
											.getWidget(i, j);
									if (lb != null) {
										String name = lb.getText();
										
										if (name.endsWith("*")) {
											if (!flag)
												((TextBox) w).setFocus(true);
											
											
											lb.setStyleName("notFilled");

											flag = true;
										}
									}
								}
								else
								{
									Label lb = (Label) TableOperations.visibleForm
											.getWidget(i, j);
									 if(lb !=null)
									 lb.setStyleName("Filled");
								}
							}
							else if (w instanceof ListBox) {
								if (((ListBox) w).getItemText(((ListBox) w).getSelectedIndex()).equals("")) {
									Label lb = (Label) TableOperations.visibleForm
											.getWidget(i, j);
									if (lb != null) {
										String name = lb.getText();
										
										if (name.endsWith("*")) {
											if (!flag)
												((ListBox) w).setFocus(true);
											
											
											lb.setStyleName("notFilled");

											flag = true;
										}
									}
								}
								else
								{
									Label lb = (Label) TableOperations.visibleForm
											.getWidget(i, j);
									 if(lb !=null)
									 lb.setStyleName("Filled");
								}
							}
						}
					}
				}

			}
		}
		if (flag) {
			Window.alert("Please enter missing values" );
			return false;
		}
		return true;
	}

}