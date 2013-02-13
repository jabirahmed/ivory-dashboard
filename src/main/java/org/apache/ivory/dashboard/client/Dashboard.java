package org.apache.ivory.dashboard.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabBar.Tab;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Dashboard implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	private final static IvoryDashboardServiceAsync service = GWT
			.create(IvoryDashboardService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		service.getEntities(new AsyncCallback<String[]>() {
			public void onSuccess(String[] entities) {
				TableOperations.visibleForm.setCellPadding(6);
				buildChoiceUI(entities);
			}

			public void onFailure(Throwable caught) {
				Window.alert("Failed to get list of entities - "
						+ caught.getMessage());
			}
		});
	}

	public static void buildChoiceUI(String[] entities) {

		/*
		 * Creating a listbox having entity choices Creating a button for
		 * creating entity
		 */
		final ListBox entitychoice = new ListBox();
		final Button createEntityButton = new Button("Create");
		final Button homeButton = new Button("Home");
		RootPanel.get("mainContainer").add(entitychoice);
		RootPanel.get("mainContainer").add(createEntityButton);
		RootPanel.get("mainContainer").add(homeButton);
		for (String entity : entities) {
			entitychoice.addItem(entity);
		}

		/*
		 * Add event listeners to create button on mouse click or pressing enter
		 * key, contact server for the entity schema definition
		 */
		class HomeButtonHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the clicks on the create Button.
			 */
			public void onClick(ClickEvent event) {

				loadHome();
			}

			/**
			 * Fired when the user presses Enter in the Create Button.
			 */
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					loadHome();
				}
			}

			private void loadHome() {
				Window.Location.reload();

			}
		}
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the clicks on the create Button.
			 */
			public void onClick(ClickEvent event) {
				getXSD(getChoice());
			}

			/**
			 * Fired when the user presses Enter in the Create Button.
			 */
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					getXSD(getChoice());
				}
			}

			/**
			 * Send the name from the nameField to the server and wait for a
			 * response.
			 */
			private String getChoice() {

				createEntityButton.setEnabled(false);
				return (entitychoice.getItemText(entitychoice
						.getSelectedIndex()));
			}

		}

		MyHandler handler = new MyHandler();
		createEntityButton.addClickHandler(handler);
		createEntityButton.addKeyUpHandler(handler);

		HomeButtonHandler homeHandler = new HomeButtonHandler();
		homeButton.addClickHandler(homeHandler);
		homeButton.addKeyUpHandler(homeHandler);

	}

	public static void getXSD(String choice) {
		service.getSchema(choice, new AsyncCallback<String>() {
			public void onFailure(Throwable e) {
				Window.alert("Failed to get schema for entity - "
						+ e.getMessage());
			}

			public void onSuccess(String schema) {
				ParseXSD.parse(schema);
			}
		});
	}
}
