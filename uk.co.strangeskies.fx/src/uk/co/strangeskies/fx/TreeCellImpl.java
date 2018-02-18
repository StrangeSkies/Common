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
 * This file is part of uk.co.strangeskies.fx.
 *
 * uk.co.strangeskies.fx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.fx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.fx;

import static uk.co.strangeskies.fx.FxUtilities.getResource;
import static uk.co.strangeskies.fx.FxmlLoadBuilder.build;
import static uk.co.strangeskies.collection.stream.StreamUtilities.reverse;
import static uk.co.strangeskies.collection.stream.StreamUtilities.throwingSerialCombiner;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.HBox;
import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * A basic tree cell implementation for {@link TreeItem}. This class may be
 * extended to provide further functionality.
 * 
 * @author Elias N Vasylenko
 */
public class TreeCellImpl extends TreeCell<TreeItemData<?>> {
	/**
	 * Load a new instance from the FXML located according to
	 * {@link FxUtilities#getResource(Class)} for this class.
	 * 
	 * @param tree
	 *          the owning tree view
	 */
	public TreeCellImpl(ModularTreeView tree) {
		build().object(this).resource(getResource(TreeCellImpl.class)).load();

		setMinWidth(0);
		prefWidth(0);

		selectedProperty().addListener(change -> {
			tree.setCellSelected(this, isSelected());
		});

		addEventHandler(KeyEvent.ANY, event -> {
			if (event.getCode() == KeyCode.CONTEXT_MENU && getGraphic() != null) {
				event.consume();

				if (event.getEventType() == KeyEvent.KEY_RELEASED) {
					Bounds sceneBounds = getGraphic().localToScene(getGraphic().getLayoutBounds());
					Bounds screenBounds = getGraphic().localToScreen(getGraphic().getLayoutBounds());

					PickResult pickResult = new PickResult(getGraphic(), sceneBounds.getMaxX(), sceneBounds.getMaxY());

					getGraphic().fireEvent(
							new ContextMenuEvent(
									ContextMenuEvent.CONTEXT_MENU_REQUESTED,
									sceneBounds.getMaxX(),
									sceneBounds.getMaxY(),
									screenBounds.getMaxX(),
									screenBounds.getMaxY(),
									true,
									pickResult));
				}
			}
		});
	}

	@Override
	protected void updateItem(TreeItemData<?> item, boolean empty) {
		super.updateItem(item, empty);

		if (empty || item == null) {
			clearItem();
		} else {
			updateItem(item);
		}
	}

	protected void clearItem() {
		setGraphic(null);
	}

	protected <T> void updateItem(TreeItemData<T> item) {
		Node content = new HBox();
		content.prefWidth(0);

		content = reverse(item.contributions(new TypeToken<TreeCellContribution<? super T>>() {}))
				.reduce(content, (c, contribution) -> contribution.configureCell(item, c), throwingSerialCombiner());

		setGraphic(content);
	}
}
