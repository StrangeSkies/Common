/*
 * Copyright (C) 2018 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.eclipse.
 *
 * uk.co.strangeskies.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.eclipse;

import static java.util.Optional.ofNullable;

import java.util.function.Predicate;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.osgi.framework.Constants;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import uk.co.strangeskies.fx.ModularTreeView;
import uk.co.strangeskies.fx.TreeContribution;
import uk.co.strangeskies.fx.TreeItemData;
import uk.co.strangeskies.fx.TreeItemImpl;

/**
 * A controller over a {@link ModularTreeView modular tree view} for use within
 * an Eclipse RCP environment.
 * <p>
 * This class allows {@link TreeContribution tree contributions} to be
 * contributed via {@link EclipseTreeContribution contributors} so that the
 * contributions are instantiated according to an Eclipse injection context.
 * 
 * @author Elias N Vasylenko
 */
public class EclipseModularTreeController {
	private final StringProperty tableId = new SimpleStringProperty();
	private final Predicate<String> filter;

	@FXML
	private ModularTreeView modularTree;

	@Inject
	private IEclipseContext context;

	/*
	 * As we are injecting into the contributions from the eclipse context of the
	 * tree we may only accept prototype scope services.
	 */
	@Inject
	@ObservableService(target = "(" + Constants.SERVICE_SCOPE + "=" + Constants.SCOPE_PROTOTYPE + ")")
	private ObservableList<EclipseTreeContribution<?>> contributions;

	@Inject
	private ESelectionService selectionService;

	/**
	 * Instantiate a controller with the default id - the simple name of the class
	 * - and no contribution filter.
	 */
	public EclipseModularTreeController() {
		tableId.set(getClass().getName());
		filter = null;
	}

	/**
	 * @param id
	 *          the {@link #getId() ID} of the controller to create
	 */
	public EclipseModularTreeController(String id) {
		tableId.set(id);
		this.filter = null;
	}

	/**
	 * @param id
	 *          the {@link #getId() ID} of the controller to create
	 * @param filter
	 *          a filter over the IDs of which {@link EclipseTreeContribution
	 *          contributions} to accept contributions from
	 */
	public EclipseModularTreeController(String id, Predicate<String> filter) {
		tableId.set(id);
		this.filter = filter;
	}

	@FXML
	void initialize() {
		contributions.addListener((ListChangeListener<EclipseTreeContribution<?>>) change -> {
			while (change.next())
				if (change.wasAdded())
					change.getAddedSubList().forEach(this::contribute);
		});

		contributions.stream().forEach(this::contribute);

		modularTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			selectionService.setSelection(ofNullable(newValue).map(TreeItem::getValue).map(TreeItemData::data).orElse(null));
		});
	}

	@PreDestroy
	void destroy() {}

	/**
	 * @return The ID property of the controller. This is used to allow
	 *         {@link EclipseTreeContribution contributions} to filter which
	 *         controllers they wish to contribute to.
	 */
	public StringProperty getTableIdProperty() {
		return tableId;
	}

	/**
	 * @return the current ID of the controller
	 */
	public String getId() {
		return tableId.get();
	}

	/**
	 * @param id
	 *          the new ID for the controller
	 */
	public void setId(String id) {
		tableId.set(id);
	}

	protected void contribute(EclipseTreeContribution<?> contribution) {
		if ((filter == null || filter.test(contribution.getContributionId())) && contribution.appliesToTree(getId())) {
			context.set(EclipseModularTreeController.class, this);
			ContextInjectionFactory.inject(contribution, context);
			modularTree.addContribution(contribution);
		}
	}

	/**
	 * @return the modular tree view instance
	 */
	public ModularTreeView getTreeView() {
		return modularTree;
	}

	/**
	 * @return the currently selected tree item
	 */
	public TreeItemImpl<?> getSelection() {
		return (TreeItemImpl<?>) modularTree.getSelectionModel().getSelectedItem();
	}

	/**
	 * @return the currently selected tree item data
	 */
	public TreeItemData<?> getSelectionData() {
		return getSelection().getValue();
	}
}
